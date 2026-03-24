import logging

import uvicorn
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from parser.config import settings
from parser.db import get_connection, initialize_schema
from parser.exceptions import AppError
from parser.logging_setup import configure_logging
from parser.models.dto import CandleImportRequest, CandleImportResponse, HealthResponse
from parser.repositories.candle_repository import CandleRepository
from parser.services.candle_import_service import CandleImportService


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

    return app


app = create_app()


if __name__ == "__main__":
    uvicorn.run("parser.main:app", host="0.0.0.0", port=settings.python_service_port, reload=False)
