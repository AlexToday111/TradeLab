import logging
from uuid import uuid4

import uvicorn
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field

from parser.candles.repositories.candle_repository import CandleRepository
from parser.common.config.db import get_connection, initialize_schema
from parser.common.config.settings import settings
from parser.common.exceptions import AppError
from parser.common.util.logging import bind_log_context, configure_logging
from parser.imports.dto.candle_import_dto import CandleImportRequest, CandleImportResponse
from parser.imports.repositories.candle_import_repository import CandleImportRepository
from parser.imports.services.candle_import_service import CandleImportService
from parser.runs.dto.run_execute_dto import RunExecuteRequest, RunExecuteResponse
from parser.runs.services.strategy_execution_service import StrategyExecutionService
from parser.strategies.dto.strategy_validation_dto import (
    StrategyValidationRequest,
    StrategyValidationResponse,
)
from parser.strategies.services.strategy_validation_service import StrategyValidationService

logger = logging.getLogger(__name__)


class HealthResponse(BaseModel):
    status: str = Field(description="Service health status.", examples=["ok"])
    service: str = Field(description="Service identifier.", examples=["python-parser"])


class ErrorResponse(BaseModel):
    status: str = Field(description="Error status.", examples=["error"])
    message: str = Field(
        description="Human-readable error message.",
        examples=["Dataset was not found"],
    )


def create_app() -> FastAPI:
    configure_logging()

    app = FastAPI(
        title="TradeLab Python Parser API",
        version="0.1.0",
        description="Internal API for candle imports, strategy validation, and strategy execution.",
        docs_url="/docs",
        redoc_url="/redoc",
        openapi_url="/openapi.json",
        openapi_tags=[
            {"name": "health", "description": "Service health endpoints."},
            {"name": "imports", "description": "Market candle import operations."},
            {"name": "strategies", "description": "Strategy validation endpoints."},
            {"name": "runs", "description": "Strategy execution endpoints."},
        ],
    )

    @app.middleware("http")
    async def add_correlation_context(request: Request, call_next):
        correlation_id = request.headers.get("X-Correlation-Id") or f"py-{uuid4().hex}"
        with bind_log_context(correlation_id=correlation_id):
            response = await call_next(request)
        response.headers["X-Correlation-Id"] = correlation_id
        return response

    @app.on_event("startup")
    async def on_startup() -> None:
        logger.info("Starting python-parser service on port %s", settings.python_service_port)
        try:
            connection = get_connection()
            try:
                initialize_schema(connection)
            finally:
                connection.close()
        except AppError:
            logger.warning("Database schema initialization skipped during startup", exc_info=True)

    @app.exception_handler(AppError)
    async def handle_app_error(_: Request, exc: AppError) -> JSONResponse:
        logger.exception("Application error: %s", exc.message)
        return JSONResponse(
            status_code=exc.status_code,
            content={"status": "error", "message": exc.message},
        )

    @app.get(
        "/health",
        response_model=HealthResponse,
        tags=["health"],
        summary="Check parser health",
        description="Returns the current status of the Python parser service.",
    )
    async def healthcheck() -> HealthResponse:
        return HealthResponse(status="ok", service="python-parser")

    @app.post(
        "/internal/import/candles",
        response_model=CandleImportResponse,
        tags=["imports"],
        summary="Import candles",
        description="Fetches candles from the configured exchange and stores them in PostgreSQL.",
        responses={400: {"model": ErrorResponse}, 500: {"model": ErrorResponse}},
    )
    async def import_candles(request: CandleImportRequest) -> CandleImportResponse:
        logger.info("Incoming candle import request")
        connection = get_connection()
        try:
            repository = CandleImportRepository(connection)
            service = CandleImportService(repository)
            return service.import_candles(request)
        finally:
            connection.close()

    @app.post(
        "/internal/strategies/validate",
        response_model=StrategyValidationResponse,
        tags=["strategies"],
        summary="Validate strategy file",
        description=(
            "Loads a strategy file and validates its exported metadata and parameters schema."
        ),
        responses={400: {"model": ErrorResponse}, 500: {"model": ErrorResponse}},
    )
    async def validate_strategy(request: StrategyValidationRequest) -> StrategyValidationResponse:
        logger.info("Incoming strategy validation request for %s", request.file_path)
        service = StrategyValidationService()
        return service.validate(request.file_path)

    @app.post(
        "/internal/runs/execute",
        response_model=RunExecuteResponse,
        tags=["runs"],
        summary="Execute strategy run",
        description=(
            "Executes a strategy against stored candles for the requested range and parameters."
        ),
        responses={400: {"model": ErrorResponse}, 500: {"model": ErrorResponse}},
    )
    async def execute_run(request: RunExecuteRequest) -> RunExecuteResponse:
        logger.info("Incoming strategy run request for %s", request.strategy_file_path)

        connection = None
        try:
            connection = get_connection()
            repository = CandleRepository(connection)
            service = StrategyExecutionService(repository)
            return service.execute(request)
        except AppError as exc:
            logger.exception("Strategy run failed with application error")
            return RunExecuteResponse(success=False, metrics=None, error=exc.message)
        except Exception:  # noqa: BLE001
            logger.exception("Strategy run failed with unexpected error")
            return RunExecuteResponse(
                success=False,
                metrics=None,
                error="Unexpected execution error",
            )
        finally:
            if connection is not None:
                connection.close()

    return app


app = create_app()


if __name__ == "__main__":
    uvicorn.run("parser.main:app", host="0.0.0.0", port=settings.python_service_port, reload=False)
