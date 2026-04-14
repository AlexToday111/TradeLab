from datetime import UTC, datetime
from decimal import Decimal

from parser.candles.models.candle import Candle


def map_moex_klines(symbol: str, interval: str, raw_klines: list[list]) -> list[Candle]:
    if not raw_klines:
        return []

    columns = raw_klines[0]
    rows = raw_klines[1:]
    if not isinstance(columns, list):
        return []

    indexed_rows = [dict(zip(columns, row, strict=False)) for row in rows]
    candles = [map_moex_kline(symbol, interval, row) for row in indexed_rows]

    unique_by_open_time: dict[datetime, Candle] = {}
    for candle in candles:
        unique_by_open_time[candle.open_time] = candle

    return sorted(unique_by_open_time.values(), key=lambda item: item.open_time)


def map_moex_kline(symbol: str, interval: str, raw_kline: dict[str, object]) -> Candle:
    open_time = _parse_moex_datetime(str(raw_kline["begin"]))
    close_time = _parse_moex_datetime(str(raw_kline["end"]))

    return Candle(
        exchange="moex",
        symbol=symbol.upper(),
        interval=interval,
        open_time=open_time,
        close_time=close_time,
        open=Decimal(str(raw_kline["open"])),
        high=Decimal(str(raw_kline["high"])),
        low=Decimal(str(raw_kline["low"])),
        close=Decimal(str(raw_kline["close"])),
        volume=Decimal(str(raw_kline.get("volume", 0))),
    )


def _parse_moex_datetime(value: str) -> datetime:
    parsed = datetime.fromisoformat(value)
    if parsed.tzinfo is None:
        return parsed.replace(tzinfo=UTC)
    return parsed.astimezone(UTC)
