from parser.common.exceptions import ExchangeError
from parser.imports.exchanges.base import BaseExchangeClient
from parser.imports.exchanges.binance.client import BinanceClient
from parser.imports.exchanges.bybit.client import BybitClient
from parser.imports.exchanges.moex.client import MoexClient


def get_exchange_client(exchange: str) -> BaseExchangeClient:
    exchange_normalized = exchange.strip().lower()

    if exchange_normalized == "binance":
        return BinanceClient()
    if exchange_normalized == "bybit":
        return BybitClient()
    if exchange_normalized == "moex":
        return MoexClient()

    raise ExchangeError(f"Unsupported exchange: {exchange}")
