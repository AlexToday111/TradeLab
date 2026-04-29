from __future__ import annotations

from decimal import Decimal

import httpx

from parser.live.exchanges.base import (
    ExchangeCredentials,
    LiveBalanceSnapshot,
    LiveExchangeAdapter,
    LiveOrderRequest,
    LiveOrderResult,
    LiveOrderStatus,
    LivePositionSnapshot,
)


class BinanceLiveExchangeAdapter(LiveExchangeAdapter):
    exchange = "binance"

    def __init__(
        self,
        base_url: str = "https://api.binance.com",
        real_order_enabled: bool = False,
    ) -> None:
        self.base_url = base_url.rstrip("/")
        self.real_order_enabled = real_order_enabled

    def get_latest_price(self, symbol: str) -> Decimal | None:
        response = httpx.get(
            f"{self.base_url}/api/v3/ticker/price",
            params={"symbol": symbol.upper()},
            timeout=5.0,
        )
        response.raise_for_status()
        return Decimal(response.json()["price"])

    def place_order(
        self,
        order_request: LiveOrderRequest,
        credentials: ExchangeCredentials,
    ) -> LiveOrderResult:
        if not self.real_order_enabled:
            return LiveOrderResult(
                exchange_order_id="binance-live-disabled",
                status=LiveOrderStatus.ACCEPTED,
            )
        return LiveOrderResult(
            exchange_order_id=None,
            status=LiveOrderStatus.REJECTED,
            rejected_reason="Python Binance live signed order submission is not enabled",
        )

    def cancel_order(self, order_id: str, credentials: ExchangeCredentials) -> None:
        return None

    def get_order(self, order_id: str, credentials: ExchangeCredentials) -> LiveOrderResult:
        return LiveOrderResult(exchange_order_id=order_id, status=LiveOrderStatus.ACCEPTED)

    def get_open_orders(self, credentials: ExchangeCredentials) -> list[LiveOrderResult]:
        return []

    def get_positions(self, credentials: ExchangeCredentials) -> list[LivePositionSnapshot]:
        return []

    def get_balances(self, credentials: ExchangeCredentials) -> list[LiveBalanceSnapshot]:
        return []

    def ping_connection(self) -> bool:
        try:
            response = httpx.get(f"{self.base_url}/api/v3/ping", timeout=5.0)
            return response.status_code < 400
        except httpx.HTTPError:
            return False

    def validate_credentials(self, credentials: ExchangeCredentials) -> bool:
        return len(credentials.api_key) >= 8 and len(credentials.api_secret) >= 8
