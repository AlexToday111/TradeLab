import logging
from datetime import UTC, datetime

from parser.common.exceptions import ValidationError
from parser.imports.dto.candle_import_dto import CandleImportRequest, CandleImportResponse
from parser.imports.exchanges.binance.mapper import map_binance_klines
from parser.imports.exchanges.factory import get_exchange_client
from parser.imports.repositories.candle_import_repository import CandleImportRepository


logger = logging.getLogger(__name__)


class CandleImportService:
    def __init__(self, candle_repository: CandleImportRepository) -> None:
        self.candle_repository = candle_repository

    def import_candles(self, request: CandleImportRequest) -> CandleImportResponse:
        exchange = request.exchange.strip().lower()
        symbol = request.symbol.strip().upper()
        interval = request.interval.strip()
        from_time = self._normalize_datetime(request.from_time)
        to_time = self._normalize_datetime(request.to_time)

        if from_time >= to_time:
            raise ValidationError("'from' must be earlier than 'to'")

        logger.info(
            "Import parameters: exchange=%s symbol=%s interval=%s from=%s to=%s",
            exchange,
            symbol,
            interval,
            from_time.isoformat(),
            to_time.isoformat(),
        )

        client = get_exchange_client(exchange)
        raw_klines = client.load_klines_raw(
            symbol=symbol,
            interval=interval,
            start_time=from_time,
            end_time=to_time,
        )
        candles = map_binance_klines(symbol=symbol, interval=interval, raw_klines=raw_klines)
        imported = self.candle_repository.save_all(candles)

        logger.info(
            "Imported candles: exchange=%s symbol=%s interval=%s imported=%s",
            exchange,
            symbol,
            interval,
            imported,
        )

        return CandleImportResponse(
            status="success",
            exchange=exchange,
            symbol=symbol,
            interval=interval,
            imported=imported,
            from_time=from_time,
            to_time=to_time,
        )

    @staticmethod
    def _normalize_datetime(value: datetime) -> datetime:
        if value.tzinfo is None:
            return value.replace(tzinfo=UTC)
        return value.astimezone(UTC)
