from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from decimal import Decimal

from backtesting.data.models import Candle
from backtesting.engine.portfolio import Portfolio
from backtesting.engine.position_sizing import PositionSizingConfig, resolve_order_quantity
from backtesting.models.order import OrderIntent, OrderSide

DECIMAL_ZERO = Decimal("0")
DECIMAL_TEN_THOUSAND = Decimal("10000")


@dataclass(frozen=True, slots=True)
class ExecutionFill:
    side: str
    quantity: float
    price: float
    fee: float
    executed_at: datetime


class ExecutionEngine:
    def __init__(
        self,
        *,
        fee_rate: float = 0.0,
        slippage_bps: float = 0.0,
        position_sizing: PositionSizingConfig | None = None,
    ) -> None:
        self._fee_rate = Decimal(str(fee_rate))
        self._slippage_bps = Decimal(str(slippage_bps))
        self._position_sizing = position_sizing or PositionSizingConfig()

    def execute(
        self,
        order_intents: list[OrderIntent],
        *,
        bar: Candle,
        portfolio: Portfolio,
    ) -> list[ExecutionFill]:
        if not order_intents:
            return []

        candle_open = Decimal(str(bar.open))
        portfolio.mark_to_market(candle_open)
        fills: list[ExecutionFill] = []

        for order_intent in order_intents:
            quantity = resolve_order_quantity(
                order_intent,
                price=candle_open,
                equity=portfolio.equity,
                current_position_size=portfolio.position_size,
                default_sizing=self._position_sizing,
            )
            if quantity == DECIMAL_ZERO:
                continue

            fill_price = self._apply_slippage(order_intent.side, candle_open)
            fee = abs(quantity * fill_price) * self._fee_rate
            portfolio.apply_fill(
                timestamp=bar.timestamp,
                side=order_intent.side,
                quantity=quantity,
                price=fill_price,
                fee=fee,
            )
            fills.append(
                ExecutionFill(
                    side=order_intent.side.value,
                    quantity=float(quantity),
                    price=float(fill_price),
                    fee=float(fee),
                    executed_at=bar.timestamp,
                )
            )

        return fills

    def _apply_slippage(self, side: OrderSide, price: Decimal) -> Decimal:
        if self._slippage_bps == DECIMAL_ZERO:
            return price

        slippage_ratio = self._slippage_bps / DECIMAL_TEN_THOUSAND
        if side is OrderSide.BUY:
            return price * (Decimal("1") + slippage_ratio)
        return price * (Decimal("1") - slippage_ratio)
