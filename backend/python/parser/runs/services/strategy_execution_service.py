import importlib.util
import logging
import sys
from datetime import UTC, datetime
from pathlib import Path
from types import ModuleType
from uuid import uuid4

from parser.candles.models.candle import Candle
from parser.candles.repositories.candle_repository import CandleRepository
from parser.runs.dto.run_execute_dto import RunExecuteRequest, RunExecuteResponse


logger = logging.getLogger(__name__)


class StrategyExecutionService:
    def __init__(self, candle_repository: CandleRepository) -> None:
        self.candle_repository = candle_repository

    def execute(self, request: RunExecuteRequest) -> RunExecuteResponse:
        exchange = request.exchange.strip().lower()
        symbol = request.symbol.strip().upper()
        interval = request.interval.strip()
        from_time = self._parse_datetime(request.from_time, "from")
        if from_time is None:
            return self._failed(f"Invalid datetime for 'from': {request.from_time}")

        to_time = self._parse_datetime(request.to_time, "to")
        if to_time is None:
            return self._failed(f"Invalid datetime for 'to': {request.to_time}")

        strategy_path = Path(request.strategy_file_path).expanduser().resolve(strict=False)

        logger.info(
            "Starting strategy run: strategy_file=%s exchange=%s symbol=%s interval=%s from=%s to=%s",
            strategy_path,
            exchange,
            symbol,
            interval,
            from_time.isoformat(),
            to_time.isoformat(),
        )

        if from_time >= to_time:
            return self._failed("'from' must be earlier than 'to'")

        if not strategy_path.exists():
            return self._failed(f"Strategy file does not exist: {strategy_path}")

        if not strategy_path.is_file():
            return self._failed(f"Strategy path is not a file: {strategy_path}")

        module_name = f"strategy_execution_{uuid4().hex}"

        try:
            try:
                module = self._load_module(module_name, strategy_path)
            except Exception as exc:  # noqa: BLE001
                logger.exception("Failed to load strategy module from %s", strategy_path)
                return self._failed(f"Failed to load strategy module: {exc}")

            strategy_class = getattr(module, "Strategy", None)
            if strategy_class is None or not isinstance(strategy_class, type):
                return self._failed("Strategy class not found")

            try:
                strategy = strategy_class()
            except Exception as exc:  # noqa: BLE001
                logger.exception("Failed to instantiate Strategy from %s", strategy_path)
                return self._failed(f"Failed to instantiate Strategy: {exc}")

            run_method = getattr(strategy, "run", None)
            if not callable(run_method):
                return self._failed("Strategy.run method not found")

            candles = self.candle_repository.find_by_market_range(
                exchange=exchange,
                symbol=symbol,
                interval=interval,
                from_time=from_time,
                to_time=to_time,
            )
            if not candles:
                return self._failed("candles not found for requested range")

            candles_payload = [self._serialize_candle(candle) for candle in candles]
            logger.info(
                "Loaded candles for strategy run: strategy_file=%s candles_count=%s",
                strategy_path,
                len(candles_payload),
            )

            try:
                result = run_method(candles_payload, request.params)
            except Exception as exc:  # noqa: BLE001
                logger.exception("Strategy.run raised exception for %s", strategy_path)
                return self._failed(f"Strategy.run raised exception: {exc}")

            if not isinstance(result, dict):
                return self._failed("invalid run result")

            if "metrics" not in result:
                return self._failed("metrics missing in result")

            metrics = result["metrics"]
            if not isinstance(metrics, dict):
                return self._failed("invalid run result")

            logger.info(
                "Strategy run completed successfully: strategy_file=%s candles_count=%s",
                strategy_path,
                len(candles_payload),
            )
            return RunExecuteResponse(success=True, metrics=metrics, error=None)
        except Exception as exc:  # noqa: BLE001
            logger.exception("Strategy execution failed for %s", strategy_path)
            return self._failed(str(exc))
        finally:
            sys.modules.pop(module_name, None)

    @staticmethod
    def _load_module(module_name: str, file_path: Path) -> ModuleType:
        spec = importlib.util.spec_from_file_location(module_name, file_path)
        if spec is None or spec.loader is None:
            raise ValueError(f"Unable to load strategy module from {file_path}")

        module = importlib.util.module_from_spec(spec)
        sys.modules[module_name] = module
        spec.loader.exec_module(module)
        return module

    @staticmethod
    def _parse_datetime(value: str, field_name: str) -> datetime | None:
        try:
            normalized = value.replace("Z", "+00:00")
            parsed = datetime.fromisoformat(normalized)
        except ValueError:
            logger.warning("Invalid datetime provided for %s: %s", field_name, value)
            return None

        if parsed.tzinfo is None:
            return parsed.replace(tzinfo=UTC)
        return parsed.astimezone(UTC)

    @staticmethod
    def _normalize_datetime(value: datetime) -> datetime:
        if value.tzinfo is None:
            return value.replace(tzinfo=UTC)
        return value.astimezone(UTC)

    @staticmethod
    def _serialize_candle(candle: Candle) -> dict[str, object]:
        return {
            "open_time": StrategyExecutionService._normalize_datetime(candle.open_time).isoformat(),
            "close_time": StrategyExecutionService._normalize_datetime(candle.close_time).isoformat(),
            "open": float(candle.open),
            "high": float(candle.high),
            "low": float(candle.low),
            "close": float(candle.close),
            "volume": float(candle.volume),
        }

    @staticmethod
    def _failed(error: str) -> RunExecuteResponse:
        logger.warning("Strategy execution failed: %s", error)
        return RunExecuteResponse(success=False, metrics=None, error=error)
