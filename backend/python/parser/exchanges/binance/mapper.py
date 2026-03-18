from datetime import datetime, timezone
from decimal import Decimal

from parser.models.candle import Candle


def map_binance_kline(symbol: str, interval: str, raw_kline: list) -> Candle:
    """
    Binance kline format:
    [
        0 open_time,
        1 open,
        2 high,
        3 low,
        4 close,
        5 volume,
        6 close_time,
        7 quote_asset_volume,
        8 number_of_trades,
        9 taker_buy_base_asset_volume,
        10 taker_buy_quote_asset_volume,
        11 ignore
    ]
    """
    open_time_ms = int(raw_kline[0])

    return Candle(
        exchange="binance",
        symbol=symbol,
        interval=interval,
        open_time=datetime.fromtimestamp(open_time_ms / 1000, tz=timezone.utc),
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

    return sorted(unique_by_open_time.values(), key=lambda c: c.open_time)