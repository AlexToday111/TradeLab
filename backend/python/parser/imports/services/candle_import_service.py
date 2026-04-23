import logging
from datetime import UTC, datetime, timedelta

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
        quality_report = self._quality_report(candles, interval=interval, raw_rows=raw_rows)
        quality_flags = [issue["code"] for issue in quality_report["issues"]]

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
            "qualityStatus": quality_report["status"],
            "qualityReport": quality_report,
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

    def _quality_report(self, candles: list, *, interval: str, raw_rows: int) -> dict[str, object]:
        checked_at = datetime.now(tz=UTC).isoformat().replace("+00:00", "Z")
        if not candles:
            return {
                "status": "FAILED",
                "checkedAt": checked_at,
                "issues": [
                    self._quality_issue(
                        code="empty_dataset",
                        severity="FAILED",
                        message="Dataset contains no canonical candles.",
                    )
                ],
            }

        issues: list[dict[str, object]] = []
        open_times = [self._normalize_datetime(candle.open_time) for candle in candles]
        if len(set(open_times)) != len(open_times):
            issues.append(
                self._quality_issue(
                    code="duplicate_candles",
                    severity="FAILED",
                    message="Duplicate candle open_time values detected.",
                )
            )

        if any(
            candle.open <= 0
            or candle.high <= 0
            or candle.low <= 0
            or candle.close <= 0
            or candle.volume < 0
            for candle in candles
        ):
            issues.append(
                self._quality_issue(
                    code="non_positive_ohlcv",
                    severity="FAILED",
                    message="OHLC values must be positive and volume must be non-negative.",
                )
            )

        if raw_rows != len(candles):
            issues.append(
                self._quality_issue(
                    code="raw_to_mapped_count_mismatch",
                    severity="WARNING",
                    message="Raw exchange rows and mapped canonical candles differ.",
                    details={"rawRows": raw_rows, "canonicalRows": len(candles)},
                )
            )

        if any(
            current >= next_time
            for current, next_time in zip(open_times, open_times[1:], strict=False)
        ):
            issues.append(
                self._quality_issue(
                    code="ordering_inconsistency",
                    severity="FAILED",
                    message="Candle open_time values are not strictly increasing.",
                )
            )

        expected_step = self._interval_to_timedelta(interval)
        if expected_step is not None and len(open_times) > 1:
            gaps = []
            mismatches = 0
            for current, next_time in zip(open_times, open_times[1:], strict=False):
                delta = next_time - current
                if delta > expected_step:
                    missing = max(1, int(delta / expected_step) - 1)
                    gaps.append(
                        {
                            "from": current.isoformat().replace("+00:00", "Z"),
                            "to": next_time.isoformat().replace("+00:00", "Z"),
                            "missingCandles": missing,
                        }
                    )
                if delta != expected_step:
                    mismatches += 1

            if gaps:
                issues.append(
                    self._quality_issue(
                        code="gaps_detected",
                        severity="WARNING",
                        message="Missing candle intervals detected.",
                        details={"gaps": gaps[:25], "totalGaps": len(gaps)},
                    )
                )
            if mismatches:
                issues.append(
                    self._quality_issue(
                        code="timeframe_inconsistency",
                        severity="WARNING",
                        message="Observed candle spacing does not always match the requested timeframe.",
                        details={"expectedStepSeconds": int(expected_step.total_seconds())},
                    )
                )

        if len(candles) < 2:
            issues.append(
                self._quality_issue(
                    code="too_small_dataset",
                    severity="WARNING",
                    message="Dataset has fewer than two candles.",
                    details={"rowCount": len(candles)},
                )
            )

        status = self._quality_status(issues)
        return {"status": status, "checkedAt": checked_at, "issues": issues}

    @staticmethod
    def _quality_issue(
        *,
        code: str,
        severity: str,
        message: str,
        details: dict[str, object] | None = None,
    ) -> dict[str, object]:
        issue: dict[str, object] = {
            "code": code,
            "severity": severity,
            "message": message,
        }
        if details:
            issue["details"] = details
        return issue

    @staticmethod
    def _quality_status(issues: list[dict[str, object]]) -> str:
        if any(issue.get("severity") == "FAILED" for issue in issues):
            return "FAILED"
        if issues:
            return "WARNING"
        return "OK"

    @staticmethod
    def _interval_to_timedelta(interval: str) -> timedelta | None:
        normalized = interval.strip().lower()
        mapping = {
            "1m": timedelta(minutes=1),
            "3m": timedelta(minutes=3),
            "5m": timedelta(minutes=5),
            "15m": timedelta(minutes=15),
            "30m": timedelta(minutes=30),
            "1h": timedelta(hours=1),
            "4h": timedelta(hours=4),
            "1d": timedelta(days=1),
            "1w": timedelta(weeks=1),
        }
        return mapping.get(normalized)

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
