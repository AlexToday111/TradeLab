from __future__ import annotations

from datetime import datetime
from typing import Any

import pandas as pd

from backtesting.engine.portfolio import Portfolio, PositionSnapshot
from backtesting.engine.position_sizing import PositionSizingConfig
from backtesting.models.order import OrderIntent, OrderSide, PositionSizingMode


class StrategyContext:
    def __init__(
        self,
        *,
        params: dict[str, Any],
        portfolio: Portfolio,
        position_sizing: PositionSizingConfig,
    ) -> None:
        self.params = dict(params)
        self._portfolio = portfolio
        self._position_sizing = position_sizing
        self._historical_data = pd.DataFrame()
        self._order_intents: list[OrderIntent] = []
        self._current_bar_time: datetime | None = None

    @property
    def current_position(self) -> PositionSnapshot | None:
        return self._portfolio.current_position

    @property
    def cash(self) -> float:
        return float(self._portfolio.cash)

    @property
    def equity(self) -> float:
        return float(self._portfolio.equity)

    @property
    def historical_data(self) -> pd.DataFrame:
        return self._historical_data.copy()

    def update_market_state(
        self,
        *,
        historical_data: pd.DataFrame,
        current_bar_time: datetime,
    ) -> None:
        self._historical_data = historical_data.copy()
        self._current_bar_time = current_bar_time

    def buy(
        self,
        *,
        quantity: float | None = None,
        size_mode: PositionSizingMode | None = None,
        size_value: float | None = None,
        metadata: dict[str, Any] | None = None,
    ) -> None:
        self._queue_order(
            side=OrderSide.BUY,
            quantity=quantity,
            size_mode=size_mode,
            size_value=size_value,
            close_position=False,
            metadata=metadata,
        )

    def sell(
        self,
        *,
        quantity: float | None = None,
        size_mode: PositionSizingMode | None = None,
        size_value: float | None = None,
        metadata: dict[str, Any] | None = None,
    ) -> None:
        self._queue_order(
            side=OrderSide.SELL,
            quantity=quantity,
            size_mode=size_mode,
            size_value=size_value,
            close_position=False,
            metadata=metadata,
        )

    def close(self, *, metadata: dict[str, Any] | None = None) -> None:
        position = self._portfolio.position_size
        if position == 0:
            return

        self._queue_order(
            side=OrderSide.SELL if position > 0 else OrderSide.BUY,
            quantity=None,
            size_mode=None,
            size_value=None,
            close_position=True,
            metadata=metadata,
        )

    def drain_order_intents(self) -> list[OrderIntent]:
        intents = list(self._order_intents)
        self._order_intents.clear()
        return intents

    def _queue_order(
        self,
        *,
        side: OrderSide,
        quantity: float | None,
        size_mode: PositionSizingMode | None,
        size_value: float | None,
        close_position: bool,
        metadata: dict[str, Any] | None,
    ) -> None:
        resolved_mode = size_mode or self._position_sizing.mode
        resolved_value = size_value if size_value is not None else self._position_sizing.value
        self._order_intents.append(
            OrderIntent(
                side=side,
                quantity=quantity,
                sizing_mode=None if quantity is not None or close_position else resolved_mode,
                sizing_value=None if quantity is not None or close_position else resolved_value,
                close_position=close_position,
                created_at=self._current_bar_time,
                metadata=metadata or {},
            )
        )
