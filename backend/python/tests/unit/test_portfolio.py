from __future__ import annotations

from datetime import UTC, datetime
from decimal import Decimal

import pytest

from backtesting.engine.portfolio import Portfolio
from backtesting.models.order import OrderSide


def test_portfolio_tracks_realized_and_unrealized_pnl():
    portfolio = Portfolio(initial_cash=1_000.0)
    entry_time = datetime(2024, 1, 1, tzinfo=UTC)
    exit_time = datetime(2024, 1, 2, tzinfo=UTC)

    portfolio.apply_fill(
        timestamp=entry_time,
        side=OrderSide.BUY,
        quantity=Decimal("2"),
        price=Decimal("100"),
        fee=Decimal("1"),
    )
    portfolio.mark_to_market(Decimal("110"))

    position = portfolio.current_position
    assert position is not None
    assert position.quantity == pytest.approx(2.0)
    assert position.entry_price == pytest.approx(100.0)
    assert position.unrealized_pnl == pytest.approx(20.0)
    assert position.equity == pytest.approx(1_019.0)

    portfolio.apply_fill(
        timestamp=exit_time,
        side=OrderSide.SELL,
        quantity=Decimal("2"),
        price=Decimal("110"),
        fee=Decimal("1"),
    )

    assert portfolio.current_position is None
    assert float(portfolio.realized_pnl) == pytest.approx(18.0)
    assert float(portfolio.cash) == pytest.approx(1_018.0)
    assert len(portfolio.trades) == 1
    assert portfolio.trades[0].pnl == pytest.approx(18.0)
    assert portfolio.trades[0].fee == pytest.approx(2.0)

