import logging

import uvicorn
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from parser.config import settings
from parser.db import get_connection, initialize_schema
from parser.exceptions import AppError
from parser.logging_setup import configure_logging
from parser.models.dto import (
    CandleImportRequest,
    CandleImportResponse,
    HealthResponse,
    RunExecuteRequest,
    RunExecuteResponse,
    StrategyValidationRequest,
    StrategyValidationResponse,
)
from parser.repositories.candle_repository import CandleRepository
from parser.services.candle_import_service import CandleImportService
from parser.services.strategy_execution_service import StrategyExecutionService
from parser.services.strategy_validation_service import StrategyValidationService


logger = logging.getLogger(__name__)


def create_app() -> FastAPI:
    configure_logging()

    app = FastAPI(title="TradeLab Python Parser", version="0.1.0")

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
        return JSONResponse(status_code=exc.status_code, content={"status": "error", "message": exc.message})

    @app.get("/health", response_model=HealthResponse)
    async def healthcheck() -> HealthResponse:
        return HealthResponse(status="ok", service="python-parser")

    @app.post("/internal/import/candles", response_model=CandleImportResponse)
    async def import_candles(request: CandleImportRequest) -> CandleImportResponse:
        logger.info("Incoming candle import request")
        connection = get_connection()
        try:
            repository = CandleRepository(connection)
            service = CandleImportService(repository)
            return service.import_candles(request)
        finally:
            connection.close()

    @app.post("/internal/strategies/validate", response_model=StrategyValidationResponse)
    async def validate_strategy(request: StrategyValidationRequest) -> StrategyValidationResponse:
        logger.info("Incoming strategy validation request for %s", request.file_path)
        service = StrategyValidationService()
        return service.validate(request.file_path)

    @app.post("/internal/runs/execute", response_model=RunExecuteResponse)
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
            return RunExecuteResponse(success=False, metrics=None, error="Unexpected execution error")
        finally:
            if connection is not None:
                connection.close()

    return app


app = create_app()


if __name__ == "__main__":
    uvicorn.run("parser.main:app", host="0.0.0.0", port=settings.python_service_port, reload=False)
