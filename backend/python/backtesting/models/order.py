from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime
from enum import StrEnum
from typing import Any


class OrderSide(StrEnum):
    BUY = "buy"
    SELL = "sell"


class OrderType(StrEnum):
    MARKET = "market"


class PositionSizingMode(StrEnum):
    FIXED_QUANTITY = "fixed_quantity"
    PERCENT_OF_EQUITY = "percent_of_equity"


@dataclass(frozen=True, slots=True)
class OrderIntent:
    side: OrderSide
    order_type: OrderType = OrderType.MARKET
    quantity: float | None = None
    sizing_mode: PositionSizingMode | None = None
    sizing_value: float | None = None
    created_at: datetime | None = None
    close_position: bool = False
    metadata: dict[str, Any] = field(default_factory=dict)

