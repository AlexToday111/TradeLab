from parser.common.exceptions import ExchangeError
from parser.imports.exchanges.base import BaseExchangeClient
from parser.imports.exchanges.binance.client import BinanceClient


def get_exchange_client(exchange: str) -> BaseExchangeClient:
    exchange_normalized = exchange.strip().lower()

    if exchange_normalized == "binance":
        return BinanceClient()

    raise ExchangeError(f"Unsupported exchange: {exchange}")
