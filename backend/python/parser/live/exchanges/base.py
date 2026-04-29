from __future__ import annotations

from abc import ABC, abstractmethod
from dataclasses import dataclass
from decimal import Decimal
from enum import Enum


class LiveOrderSide(str, Enum):
    BUY = "BUY"
    SELL = "SELL"


class LiveOrderType(str, Enum):
    MARKET = "MARKET"
    LIMIT = "LIMIT"


class LiveOrderStatus(str, Enum):
    CREATED = "CREATED"
    SUBMITTED = "SUBMITTED"
    ACCEPTED = "ACCEPTED"
    PARTIALLY_FILLED = "PARTIALLY_FILLED"
    FILLED = "FILLED"
    CANCELED = "CANCELED"
    REJECTED = "REJECTED"
    FAILED = "FAILED"


@dataclass(frozen=True)
class ExchangeCredentials:
    api_key: str
    api_secret: str


@dataclass(frozen=True)
class LiveOrderRequest:
    symbol: str
    side: LiveOrderSide
    type: LiveOrderType
    quantity: Decimal
    requested_price: Decimal | None = None


@dataclass(frozen=True)
class LiveOrderResult:
    exchange_order_id: str | None
    status: LiveOrderStatus
    executed_price: Decimal | None = None
    rejected_reason: str | None = None


@dataclass(frozen=True)
class LivePositionSnapshot:
    symbol: str
    quantity: Decimal
    average_entry_price: Decimal
    realized_pnl: Decimal
    unrealized_pnl: Decimal


@dataclass(frozen=True)
class LiveBalanceSnapshot:
    asset: str
    free: Decimal
    locked: Decimal


class LiveExchangeAdapter(ABC):
    """Exchange adapter contract for controlled live trading.

    Implementations must not log raw credentials and must surface failures as explicit
    rejected or failed order results instead of silent best-effort behavior.
    """

    exchange: str

    @abstractmethod
    def get_latest_price(self, symbol: str) -> Decimal | None:
        raise NotImplementedError

    @abstractmethod
    def place_order(
        self,
        order_request: LiveOrderRequest,
        credentials: ExchangeCredentials,
    ) -> LiveOrderResult:
        raise NotImplementedError

    @abstractmethod
    def cancel_order(self, order_id: str, credentials: ExchangeCredentials) -> None:
        raise NotImplementedError

    @abstractmethod
    def get_order(self, order_id: str, credentials: ExchangeCredentials) -> LiveOrderResult:
        raise NotImplementedError

    @abstractmethod
    def get_open_orders(self, credentials: ExchangeCredentials) -> list[LiveOrderResult]:
        raise NotImplementedError

    @abstractmethod
    def get_positions(self, credentials: ExchangeCredentials) -> list[LivePositionSnapshot]:
        raise NotImplementedError

    @abstractmethod
    def get_balances(self, credentials: ExchangeCredentials) -> list[LiveBalanceSnapshot]:
        raise NotImplementedError

    @abstractmethod
    def ping_connection(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def validate_credentials(self, credentials: ExchangeCredentials) -> bool:
        raise NotImplementedError
