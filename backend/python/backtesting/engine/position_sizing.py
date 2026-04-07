from __future__ import annotations

from dataclasses import dataclass
from decimal import Decimal

from backtesting.models.order import OrderIntent, PositionSizingMode

DECIMAL_ZERO = Decimal("0")


@dataclass(frozen=True, slots=True)
class PositionSizingConfig:
    mode: PositionSizingMode = PositionSizingMode.FIXED_QUANTITY
    value: float = 1.0


def resolve_order_quantity(
    order_intent: OrderIntent,
    *,
    price: Decimal,
    equity: Decimal,
    current_position_size: Decimal,
    default_sizing: PositionSizingConfig,
) -> Decimal:
    if price <= DECIMAL_ZERO:
        raise ValueError("Execution price must be positive")

    if order_intent.close_position:
        return abs(current_position_size)

    if order_intent.quantity is not None:
        quantity = Decimal(str(order_intent.quantity))
        if quantity <= DECIMAL_ZERO:
            raise ValueError("Explicit quantity must be positive")
        return quantity

    sizing_mode = order_intent.sizing_mode or default_sizing.mode
    sizing_value = (
        Decimal(str(order_intent.sizing_value))
        if order_intent.sizing_value is not None
        else Decimal(str(default_sizing.value))
    )
    if sizing_value <= DECIMAL_ZERO:
        raise ValueError("Sizing value must be positive")

    if sizing_mode is PositionSizingMode.FIXED_QUANTITY:
        return sizing_value

    if sizing_mode is PositionSizingMode.PERCENT_OF_EQUITY:
        return (equity * sizing_value) / price

    raise ValueError(f"Unsupported sizing mode: {sizing_mode}")
