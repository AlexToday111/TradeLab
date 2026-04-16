import logging
from datetime import UTC, datetime

from parser.common.exceptions import ValidationError
from parser.imports.dto.candle_import_dto import (
    CandleImportRequest,
    CandleImportResponse,
    make_dataset_fingerprint,
    make_dataset_id,
)
from parser.imports.exchanges.binance.mapper import map_binance_klines
from parser.imports.exchanges.bybit.mapper import map_bybit_klines
from parser.imports.exchanges.factory import get_exchange_client
from parser.imports.exchanges.moex.mapper import map_moex_klines
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
            interval=self._normalize_interval(exchange, interval),
            start_time=from_time,
            end_time=to_time,
            category=request.category,
            engine=request.engine,
            market=request.market,
            board=request.board,
        )
        candles = self._map_klines(
            exchange=exchange,
            symbol=symbol,
            interval=interval,
            raw_klines=raw_klines,
        )
        imported_at = datetime.now(tz=UTC)
        raw_rows_count = self._count_raw_rows(exchange, raw_klines)
        dataset_metadata = self._build_dataset_metadata(
            exchange=exchange,
            symbol=symbol,
            interval=interval,
            from_time=from_time,
            to_time=to_time,
            imported_at=imported_at,
            candles=candles,
            raw_rows=raw_rows_count,
            source_options={
                "category": request.category,
                "engine": request.engine,
                "market": request.market,
                "board": request.board,
            },
        )
        imported = self.candle_repository.save_all(candles)

        logger.info(
            (
                "Imported candles: exchange=%s symbol=%s interval=%s imported=%s "
                "dataset_id=%s fingerprint=%s"
            ),
            exchange,
            symbol,
            interval,
            imported,
            dataset_metadata["datasetId"],
            dataset_metadata["fingerprint"],
        )

        return CandleImportResponse(
            status="success",
            exchange=exchange,
            symbol=symbol,
            interval=interval,
            imported=imported,
            from_time=from_time,
            to_time=to_time,
            dataset=dataset_metadata,
        )

    @staticmethod
    def _normalize_datetime(value: datetime) -> datetime:
        if value.tzinfo is None:
            return value.replace(tzinfo=UTC)
        return value.astimezone(UTC)

    def _build_dataset_metadata(
        self,
        *,
        exchange: str,
        symbol: str,
        interval: str,
        from_time: datetime,
        to_time: datetime,
        imported_at: datetime,
        candles: list,
        raw_rows: int,
        source_options: dict[str, object | None],
    ) -> dict[str, object]:
        rows_count = len(candles)
        start_at = self._normalize_datetime(candles[0].open_time) if candles else from_time
        end_at = self._normalize_datetime(candles[-1].close_time) if candles else to_time
        fingerprint = make_dataset_fingerprint(
            [
                exchange,
                symbol,
                interval,
                from_time.isoformat(),
                to_time.isoformat(),
                start_at.isoformat(),
                end_at.isoformat(),
                str(rows_count),
            ]
        )
        quality_flags = self._quality_flags(candles, raw_rows=raw_rows)

        return {
            "datasetId": make_dataset_id(exchange, symbol, interval, fingerprint),
            "source": exchange,
            "symbol": symbol,
            "timeframe": interval,
            "importedAt": imported_at.isoformat().replace("+00:00", "Z"),
            "rowsCount": rows_count,
            "startAt": start_at.isoformat().replace("+00:00", "Z"),
            "endAt": end_at.isoformat().replace("+00:00", "Z"),
            "version": fingerprint,
            "fingerprint": fingerprint,
            "qualityFlags": quality_flags,
            "lineage": {
                "importRange": {
                    "from": from_time.isoformat().replace("+00:00", "Z"),
                    "to": to_time.isoformat().replace("+00:00", "Z"),
                },
                "rawRows": raw_rows,
                "exchangeClient": exchange,
                "sourceOptions": {
                    key: value for key, value in source_options.items() if value is not None
                },
            },
        }

    def _quality_flags(self, candles: list, *, raw_rows: int) -> list[str]:
        if not candles:
            return ["empty_dataset"]

        flags: list[str] = []
        open_times = [self._normalize_datetime(candle.open_time) for candle in candles]
        if len(set(open_times)) != len(open_times):
            flags.append("duplicate_open_time")

        if any(
            candle.open <= 0
            or candle.high <= 0
            or candle.low <= 0
            or candle.close <= 0
            or candle.volume < 0
            for candle in candles
        ):
            flags.append("non_positive_ohlcv")

        if raw_rows != len(candles):
            flags.append("raw_to_mapped_count_mismatch")

        if any(
            current >= next_time
            for current, next_time in zip(open_times, open_times[1:], strict=False)
        ):
            flags.append("non_monotonic_open_time")

        return flags

    def _map_klines(
        self,
        *,
        exchange: str,
        symbol: str,
        interval: str,
        raw_klines: list,
    ) -> list:
        if exchange == "binance":
            return map_binance_klines(symbol=symbol, interval=interval, raw_klines=raw_klines)
        if exchange == "bybit":
            return map_bybit_klines(symbol=symbol, interval=interval, raw_klines=raw_klines)
        if exchange == "moex":
            return map_moex_klines(symbol=symbol, interval=interval, raw_klines=raw_klines)
        raise ValidationError(f"Unsupported exchange mapper: {exchange}")

    def _normalize_interval(self, exchange: str, interval: str) -> str:
        normalized = interval.strip().lower()
        if exchange == "bybit":
            mapping = {
                "1m": "1",
                "3m": "3",
                "5m": "5",
                "15m": "15",
                "30m": "30",
                "1h": "60",
                "4h": "240",
                "1d": "D",
                "1w": "W",
                "1mo": "M",
            }
            if normalized not in mapping:
                raise ValidationError(f"Unsupported Bybit interval: {interval}")
            return mapping[normalized]
        return normalized

    def _count_raw_rows(self, exchange: str, raw_klines: list) -> int:
        if exchange == "moex" and raw_klines:
            return max(0, len(raw_klines) - 1)
        return len(raw_klines)
