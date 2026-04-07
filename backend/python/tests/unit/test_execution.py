from __future__ import annotations

from datetime import UTC, datetime

import pytest

from backtesting.data.models import Candle
from backtesting.engine.execution import ExecutionEngine
from backtesting.engine.portfolio import Portfolio
from backtesting.engine.position_sizing import PositionSizingConfig
from backtesting.models.order import OrderIntent, OrderSide, PositionSizingMode


def test_execution_applies_next_open_slippage_and_fee():
    portfolio = Portfolio(initial_cash=1_000.0)
    engine = ExecutionEngine(fee_rate=0.01, slippage_bps=10)
    bar = Candle(
        timestamp=datetime(2024, 1, 2, tzinfo=UTC),
        open=100.0,
        high=101.0,
        low=99.0,
        close=100.0,
        volume=10.0,
    )

    fills = engine.execute(
        [OrderIntent(side=OrderSide.BUY, quantity=1.0)],
        bar=bar,
        portfolio=portfolio,
    )

    assert len(fills) == 1
    assert fills[0].price == pytest.approx(100.1)
    assert fills[0].fee == pytest.approx(1.001)
    assert float(portfolio.cash) == pytest.approx(898.899)
    assert float(portfolio.position_size) == pytest.approx(1.0)


def test_execution_resolves_percent_of_equity_quantity():
    portfolio = Portfolio(initial_cash=1_000.0)
    engine = ExecutionEngine(
        position_sizing=PositionSizingConfig(
            mode=PositionSizingMode.PERCENT_OF_EQUITY,
            value=0.5,
        )
    )
    bar = Candle(
        timestamp=datetime(2024, 1, 2, tzinfo=UTC),
        open=100.0,
        high=100.0,
        low=100.0,
        close=100.0,
        volume=10.0,
    )

    fills = engine.execute(
        [OrderIntent(side=OrderSide.BUY)],
        bar=bar,
        portfolio=portfolio,
    )

    assert len(fills) == 1
    assert fills[0].quantity == pytest.approx(5.0)
    assert float(portfolio.cash) == pytest.approx(500.0)

