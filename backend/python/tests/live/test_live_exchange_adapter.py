from decimal import Decimal

import pytest

from parser.live.exchanges.base import (
    ExchangeCredentials,
    LiveExchangeAdapter,
    LiveOrderRequest,
    LiveOrderSide,
    LiveOrderStatus,
    LiveOrderType,
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
