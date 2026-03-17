from datetime import datetime

from parser.exchanges.factory import get_exchange_client
from parser.exchanges.binance.mapper import map_binance_klines
from parser.repositories.candle_repository import CandleRepository


class CandleImportService:
    def __init__(self, candle_repository: CandleRepository) -> None:
        self.candle_repository = candle_repository

    def import_candles(
        self,
        exchange: str,
        symbol: str,
        interval: str,
        limit_total: int | None = None,
        start_time: datetime | None = None,
        end_time: datetime | None = None,
    ) -> int:
        client = get_exchange_client(exchange)
        raw_klines = client.load_klines_raw(
            symbol=symbol,
            interval=interval,
            limit_total=limit_total,
            start_time=start_time,
            end_time=end_time,
        )

        if exchange.strip().lower() == "binance":
            candles = map_binance_klines(
                symbol=symbol,
                interval=interval,
                raw_klines=raw_klines,
            )
        else:
            raise ValueError(f"Mapper not implemented for exchange: {exchange}")

        return self.candle_repository.save_all(candles)
