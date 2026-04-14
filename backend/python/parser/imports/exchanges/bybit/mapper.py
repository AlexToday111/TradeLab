from datetime import datetime, timezone
from decimal import Decimal

from parser.candles.models.candle import Candle


def map_bybit_kline(symbol: str, interval: str, raw_kline: list) -> Candle:
    open_time_ms = int(raw_kline[0])
    close_time = _resolve_close_time(open_time_ms, interval)

    return Candle(
        exchange="bybit",
        symbol=symbol,
        interval=interval,
        open_time=datetime.fromtimestamp(open_time_ms / 1000, tz=timezone.utc),
        close_time=close_time,
        open=Decimal(str(raw_kline[1])),
        high=Decimal(str(raw_kline[2])),
        low=Decimal(str(raw_kline[3])),
        close=Decimal(str(raw_kline[4])),
        volume=Decimal(str(raw_kline[5])),
    )


def map_bybit_klines(symbol: str, interval: str, raw_klines: list[list]) -> list[Candle]:
    candles = [map_bybit_kline(symbol, interval, raw_kline) for raw_kline in raw_klines]

    unique_by_open_time: dict[datetime, Candle] = {}
    for candle in candles:
        unique_by_open_time[candle.open_time] = candle

    return sorted(unique_by_open_time.values(), key=lambda item: item.open_time)


def _resolve_close_time(open_time_ms: int, interval: str) -> datetime:
    interval_value = interval.strip().upper()
    if interval_value == "D":
        delta_seconds = 24 * 60 * 60
    elif interval_value == "W":
        delta_seconds = 7 * 24 * 60 * 60
    elif interval_value == "M":
        delta_seconds = 30 * 24 * 60 * 60
    else:
        delta_seconds = int(interval_value) * 60

    close_time_ms = open_time_ms + delta_seconds * 1000
    return datetime.fromtimestamp(close_time_ms / 1000, tz=timezone.utc)
