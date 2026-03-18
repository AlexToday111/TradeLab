from parser.exchanges.base import BaseExchangeClient
from parser.exchanges.binance.client import BinanceClient


def get_exchange_client(exchange: str) -> BaseExchangeClient:
    exchange_normalized = exchange.strip().lower()

    if exchange_normalized == "binance":
        return BinanceClient()

    raise ValueError(f"Unsupported exchange: {exchange}")