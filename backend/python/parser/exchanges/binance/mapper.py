from datetime import datetime, timezone
from decimal import Decimal

from parser.models.candle import Candle


def map_binance_kline(symbol: str, interval: str, raw_kline: list) -> Candle:
    open_time_ms = int(raw_kline[0])
    close_time_ms = int(raw_kline[6])

    return Candle(
        exchange="binance",
        symbol=symbol,
        interval=interval,
        open_time=datetime.fromtimestamp(open_time_ms / 1000, tz=timezone.utc),
        close_time=datetime.fromtimestamp(close_time_ms / 1000, tz=timezone.utc),
        open=Decimal(str(raw_kline[1])),
        high=Decimal(str(raw_kline[2])),
        low=Decimal(str(raw_kline[3])),
        close=Decimal(str(raw_kline[4])),
        volume=Decimal(str(raw_kline[5])),
    )


def map_binance_klines(symbol: str, interval: str, raw_klines: list[list]) -> list[Candle]:
    candles = [map_binance_kline(symbol, interval, raw_kline) for raw_kline in raw_klines]

    unique_by_open_time: dict[datetime, Candle] = {}
    for candle in candles:
        unique_by_open_time[candle.open_time] = candle

    return sorted(unique_by_open_time.values(), key=lambda item: item.open_time)
