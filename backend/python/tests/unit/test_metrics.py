from __future__ import annotations

from datetime import UTC, datetime, timedelta

import pytest

from backtesting.metrics.performance import PerformanceMetricsCalculator
from backtesting.models.result import EquityPoint
from backtesting.models.trade import Trade


def test_metrics_calculator_computes_summary_values():
    start = datetime(2024, 1, 1, tzinfo=UTC)
    equity_curve = [
        EquityPoint(timestamp=start, cash=1_000.0, equity=1_000.0, position_size=0.0),
        EquityPoint(
            timestamp=start + timedelta(days=1),
            cash=1_000.0,
            equity=1_100.0,
            position_size=1.0,
        ),
        EquityPoint(
            timestamp=start + timedelta(days=2),
            cash=900.0,
            equity=900.0,
            position_size=0.0,
        ),
    ]
    trades = [
        Trade(
            entry_time=start,
            exit_time=start + timedelta(days=1),
            entry_price=100.0,
            exit_price=110.0,
            qty=1.0,
            pnl=10.0,
            fee=1.0,
        ),
        Trade(
            entry_time=start + timedelta(days=1),
            exit_time=start + timedelta(days=2),
            entry_price=110.0,
            exit_price=100.0,
            qty=1.0,
            pnl=-5.0,
            fee=1.0,
        ),
        Trade(
            entry_time=start + timedelta(days=2),
            exit_time=start + timedelta(days=3),
            entry_price=100.0,
            exit_price=120.0,
            qty=1.0,
            pnl=20.0,
            fee=1.0,
        ),
    ]

    summary = PerformanceMetricsCalculator().calculate(
        starting_equity=1_000.0,
        equity_curve=equity_curve,
        trades=trades,
    )

    assert summary["total_return"] == pytest.approx(-0.1)
    assert summary["net_profit"] == pytest.approx(-100.0)
    assert summary["max_drawdown"] == pytest.approx(0.1818181818)
    assert summary["win_rate"] == pytest.approx(2 / 3)
    assert summary["profit_factor"] == pytest.approx(6.0)
    assert summary["avg_win"] == pytest.approx(15.0)
    assert summary["avg_loss"] == pytest.approx(-5.0)
    assert summary["number_of_trades"] == 3

