from datetime import UTC, datetime
from decimal import Decimal
from pathlib import Path

from parser.candles.models.candle import Candle
from parser.runs.dto.run_execute_dto import RunExecuteRequest
from parser.runs.services.strategy_execution_service import StrategyExecutionService


class FakeCandleRepository:
    def __init__(self, candles):
        self.candles = candles
        self.calls = []

    def find_by_market_range(self, **kwargs):
        self.calls.append(kwargs)
        return self.candles


def build_request(strategy_file_path: str, **overrides) -> RunExecuteRequest:
    payload = {
        "strategyFilePath": strategy_file_path,
        "exchange": " Binance ",
        "symbol": " btcusdt ",
        "interval": "1h",
        "from": "2024-01-01T00:00:00Z",
        "to": "2024-01-01T02:00:00Z",
        "params": {"fast": 9},
    }
    payload.update(overrides)
    return RunExecuteRequest(**payload)


def write_strategy(tmp_path: Path, name: str, body: str) -> str:
    file_path = tmp_path / name
    file_path.write_text(body, encoding="utf-8")
    return str(file_path)


def sample_candle() -> Candle:
    return Candle(
        exchange="binance",
        symbol="BTCUSDT",
        interval="1h",
        open_time=datetime(2024, 1, 1, 0, 0, 0, tzinfo=UTC),
        close_time=datetime(2024, 1, 1, 1, 0, 0, tzinfo=UTC),
        open=Decimal("1.0"),
        high=Decimal("2.0"),
        low=Decimal("0.5"),
        close=Decimal("1.5"),
        volume=Decimal("10.0"),
    )


def test_execute_returns_metrics_for_valid_strategy(tmp_path):
    strategy_path = write_strategy(
        tmp_path,
        "valid_strategy.py",
        """
class Strategy:
    def run(self, candles, params):
        assert candles[0]["open"] == 1.0
        return {"metrics": {"total_return": params["fast"]}}
""".strip(),
    )
    repository = FakeCandleRepository([sample_candle()])

    response = StrategyExecutionService(repository).execute(build_request(strategy_path))

    assert response.success is True
    assert response.metrics == {"total_return": 9}
    assert response.error is None
    assert repository.calls[0]["exchange"] == "binance"
    assert repository.calls[0]["symbol"] == "BTCUSDT"


def test_execute_rejects_invalid_datetime(tmp_path):
    strategy_path = write_strategy(
        tmp_path,
        "valid_strategy.py",
        """
class Strategy:
    def run(self, candles, params):
        return {"metrics": {"total_return": 1}}
""".strip(),
    )
    repository = FakeCandleRepository([sample_candle()])

    response = StrategyExecutionService(repository).execute(
        build_request(strategy_path, **{"from": "not-a-date"})
    )

    assert response.success is False
    assert response.error == "Invalid datetime for 'from': not-a-date"


def test_execute_rejects_strategy_result_without_metrics(tmp_path):
    strategy_path = write_strategy(
        tmp_path,
        "invalid_result_strategy.py",
        """
class Strategy:
    def run(self, candles, params):
        return {"summary": {}}
""".strip(),
    )
    repository = FakeCandleRepository([sample_candle()])

    response = StrategyExecutionService(repository).execute(build_request(strategy_path))

    assert response.success is False
    assert response.error == "metrics missing in result"
