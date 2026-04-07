from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from decimal import Decimal

from backtesting.models.order import OrderSide
from backtesting.models.trade import Trade

DECIMAL_ZERO = Decimal("0")


def _sign(value: Decimal) -> int:
    if value > DECIMAL_ZERO:
        return 1
    if value < DECIMAL_ZERO:
        return -1
    return 0


@dataclass(slots=True)
class OpenLot:
    quantity: Decimal
    entry_price: Decimal
    entry_time: datetime
    entry_fee: Decimal


@dataclass(frozen=True, slots=True)
class PositionSnapshot:
    quantity: float
    entry_price: float
    realized_pnl: float
    unrealized_pnl: float
    equity: float


class Portfolio:
    def __init__(self, initial_cash: float) -> None:
        self._cash = Decimal(str(initial_cash))
        self._realized_pnl = DECIMAL_ZERO
        self._unrealized_pnl = DECIMAL_ZERO
        self._market_price = DECIMAL_ZERO
        self._trades: list[Trade] = []
        self._open_lots: list[OpenLot] = []
        self._total_fees = DECIMAL_ZERO

    @property
    def cash(self) -> Decimal:
        return self._cash

    @property
    def realized_pnl(self) -> Decimal:
        return self._realized_pnl

    @property
    def unrealized_pnl(self) -> Decimal:
        return self._unrealized_pnl

    @property
    def total_fees(self) -> Decimal:
        return self._total_fees

    @property
    def trades(self) -> list[Trade]:
        return list(self._trades)

    @property
    def position_size(self) -> Decimal:
        return sum((lot.quantity for lot in self._open_lots), DECIMAL_ZERO)

    @property
    def entry_price(self) -> Decimal:
        position_size = self.position_size
        if position_size == DECIMAL_ZERO:
            return DECIMAL_ZERO

        total_quantity = sum((abs(lot.quantity) for lot in self._open_lots), DECIMAL_ZERO)
        total_cost = sum(
            (abs(lot.quantity) * lot.entry_price for lot in self._open_lots),
            DECIMAL_ZERO,
        )
        if total_quantity == DECIMAL_ZERO:
            return DECIMAL_ZERO
        return total_cost / total_quantity

    @property
    def equity(self) -> Decimal:
        return self._cash + (self.position_size * self._market_price)

    @property
    def current_position(self) -> PositionSnapshot | None:
        if self.position_size == DECIMAL_ZERO:
            return None

        return PositionSnapshot(
            quantity=float(self.position_size),
            entry_price=float(self.entry_price),
            realized_pnl=float(self._realized_pnl),
            unrealized_pnl=float(self._unrealized_pnl),
            equity=float(self.equity),
        )

    def mark_to_market(self, price: Decimal) -> None:
        self._market_price = price
        self._unrealized_pnl = sum(
            (
                lot.quantity * (self._market_price - lot.entry_price)
                for lot in self._open_lots
            ),
            DECIMAL_ZERO,
        )

    def apply_fill(
        self,
        *,
        timestamp: datetime,
        side: OrderSide,
        quantity: Decimal,
        price: Decimal,
        fee: Decimal,
    ) -> list[Trade]:
        if quantity <= DECIMAL_ZERO:
            raise ValueError("Filled quantity must be positive")

        signed_quantity = quantity if side is OrderSide.BUY else -quantity
        self._cash -= signed_quantity * price
        self._cash -= fee
        self._total_fees += fee

        order_size = abs(signed_quantity)
        remaining = signed_quantity
        remaining_fee = fee
        closed_trades: list[Trade] = []

        while (
            remaining != DECIMAL_ZERO
            and self._open_lots
            and _sign(self._open_lots[0].quantity) != _sign(remaining)
        ):
            lot = self._open_lots[0]
            lot_size = abs(lot.quantity)
            close_size = min(abs(remaining), lot_size)
            exit_fee_share = fee * (close_size / order_size)
            entry_fee_share = lot.entry_fee * (close_size / lot_size)

            gross_pnl = (
                close_size * (price - lot.entry_price)
                if lot.quantity > DECIMAL_ZERO
                else close_size * (lot.entry_price - price)
            )
            total_fee = entry_fee_share + exit_fee_share
            net_pnl = gross_pnl - total_fee
            self._realized_pnl += net_pnl

            closed_trades.append(
                Trade(
                    entry_time=lot.entry_time,
                    exit_time=timestamp,
                    entry_price=float(lot.entry_price),
                    exit_price=float(price),
                    qty=float(close_size),
                    pnl=float(net_pnl),
                    fee=float(total_fee),
                )
            )

            lot.quantity -= Decimal(_sign(lot.quantity)) * close_size
            lot.entry_fee -= entry_fee_share
            remaining -= Decimal(_sign(remaining)) * close_size
            remaining_fee -= exit_fee_share

            if lot.quantity == DECIMAL_ZERO:
                self._open_lots.pop(0)

        if remaining != DECIMAL_ZERO:
            self._open_lots.append(
                OpenLot(
                    quantity=remaining,
                    entry_price=price,
                    entry_time=timestamp,
                    entry_fee=remaining_fee,
                )
            )

        self._trades.extend(closed_trades)
        self.mark_to_market(price)
        return closed_trades

