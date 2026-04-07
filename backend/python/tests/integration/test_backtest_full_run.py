from __future__ import annotations

from pathlib import Path

import pandas as pd

from backtesting.engine.backtest_engine import BacktestConfig, BacktestEngine
from backtesting.engine.position_sizing import PositionSizingConfig
from backtesting.models.order import PositionSizingMode


def write_market_data(tmp_path: Path) -> Path:
    data = pd.DataFrame(
        [
            {
                "timestamp": "2024-01-01T00:00:00Z",
                "open": 10,
                "high": 10,
                "low": 10,
                "close": 10,
                "volume": 100,
            },
            {
                "timestamp": "2024-01-02T00:00:00Z",
                "open": 10,
                "high": 10,
                "low": 10,
                "close": 10,
                "volume": 100,
            },
            {
                "timestamp": "2024-01-03T00:00:00Z",
                "open": 10,
                "high": 11,
                "low": 10,
                "close": 11,
                "volume": 100,
            },
            {
                "timestamp": "2024-01-04T00:00:00Z",
                "open": 11,
                "high": 12,
                "low": 11,
                "close": 12,
                "volume": 100,
            },
            {
                "timestamp": "2024-01-05T00:00:00Z",
                "open": 9,
                "high": 9,
                "low": 9,
                "close": 9,
                "volume": 100,
            },
            {
                "timestamp": "2024-01-06T00:00:00Z",
                "open": 9,
                "high": 9,
                "low": 8,
                "close": 8,
                "volume": 100,
            },
            {
                "timestamp": "2024-01-07T00:00:00Z",
                "open": 10,
                "high": 10,
                "low": 10,
                "close": 10,
                "volume": 100,
            },
        ]
    )
    path = tmp_path / "market.csv"
    data.to_csv(path, index=False)
    return path


def write_strategy(tmp_path: Path) -> Path:
    path = tmp_path / "ma_crossover.py"
    path.write_text(
        """
from backtesting.strategy.base import Strategy as BaseStrategy


class Strategy(BaseStrategy):
    def initialize(self, context):
        return None

    def on_bar(self, bar, context):
        history = context.historical_data
        slow_window = int(self.params["slow"])
        fast_window = int(self.params["fast"])
        if len(history) < slow_window:
            return

        fast = history["close"].rolling(fast_window).mean().iloc[-1]
        slow = history["close"].rolling(slow_window).mean().iloc[-1]
        position = context.current_position

        if fast > slow and position is None:
            context.buy()
        elif fast < slow and position is not None:
            context.close()

    def finalize(self, context):
        return None
""".strip(),
        encoding="utf-8",
    )
    return path


def test_backtest_full_run_generates_trades_and_metrics(tmp_path):
    engine = BacktestEngine(
        config=BacktestConfig(
            initial_cash=1_000.0,
            position_sizing=PositionSizingConfig(
                mode=PositionSizingMode.FIXED_QUANTITY,
                value=1.0,
            ),
        )
    )

    result = engine.run_from_files(
        data_path=str(write_market_data(tmp_path)),
        strategy_path=str(write_strategy(tmp_path)),
        strategy_params={"fast": 2, "slow": 3},
    )

    assert result.summary["number_of_trades"] >= 1
    assert result.equity_curve[0].equity != result.equity_curve[-1].equity
    assert "total_return" in result.summary
    assert "max_drawdown" in result.summary
    assert result.trades[0].entry_time.isoformat() == "2024-01-04T00:00:00+00:00"
    assert result.trades[0].exit_time.isoformat() == "2024-01-06T00:00:00+00:00"
    assert result.logs
