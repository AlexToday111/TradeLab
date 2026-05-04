from decimal import Decimal

import pytest

from parser.live.exchanges.base import (
    ExchangeCredentials,
    LiveBalanceSnapshot,
    LiveExchangeAdapter,
    LiveOrderRequest,
    LiveOrderSide,
    LiveOrderStatus,
    LiveOrderType,
    LivePositionSnapshot,
)
from parser.live.exchanges.binance import BinanceLiveExchangeAdapter


def test_binance_live_adapter_implements_contract() -> None:
    adapter = BinanceLiveExchangeAdapter(real_order_enabled=False)

    assert isinstance(adapter, LiveExchangeAdapter)
    assert adapter.exchange == "binance"


def test_binance_live_adapter_validates_credentials_without_exposing_values() -> None:
    adapter = BinanceLiveExchangeAdapter(real_order_enabled=False)

    assert adapter.validate_credentials(ExchangeCredentials("abcdefgh", "ijklmnop")) is True
    assert adapter.validate_credentials(ExchangeCredentials("short", "ijklmnop")) is False


def test_binance_live_adapter_blocks_real_submission_by_default() -> None:
    adapter = BinanceLiveExchangeAdapter(real_order_enabled=False)

    result = adapter.place_order(
        LiveOrderRequest(
            symbol="BTCUSDT",
            side=LiveOrderSide.BUY,
            type=LiveOrderType.MARKET,
            quantity=Decimal("0.01"),
        ),
        ExchangeCredentials("abcdefgh", "ijklmnop"),
    )

    assert result.status == LiveOrderStatus.ACCEPTED
    assert result.exchange_order_id == "binance-live-disabled"
    assert result.rejected_reason is None


def test_live_exchange_adapter_is_abstract() -> None:
    with pytest.raises(TypeError):
        LiveExchangeAdapter()


def test_binance_live_adapter_returns_empty_read_only_snapshots_by_default() -> None:
    adapter = BinanceLiveExchangeAdapter(real_order_enabled=False)
    credentials = ExchangeCredentials("abcdefgh", "ijklmnop")

    assert adapter.get_open_orders(credentials) == []
    assert adapter.get_positions(credentials) == []
    assert adapter.get_balances(credentials) == []


def test_live_snapshot_contracts_are_decimal_safe() -> None:
    balance = LiveBalanceSnapshot(asset="USDT", free=Decimal("10.5"), locked=Decimal("0.1"))
    position = LivePositionSnapshot(
        symbol="BTCUSDT",
        quantity=Decimal("0.01"),
        average_entry_price=Decimal("42000"),
        realized_pnl=Decimal("1.25"),
        unrealized_pnl=Decimal("-0.5"),
    )

    assert balance.free + balance.locked == Decimal("10.6")
    assert position.symbol == "BTCUSDT"
    assert position.unrealized_pnl == Decimal("-0.5")
