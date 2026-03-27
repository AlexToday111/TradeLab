from datetime import datetime
from typing import Sequence

import psycopg

from parser.exceptions import RepositoryError
from parser.models.candle import Candle


class CandleRepository:
    def __init__(self, connection) -> None:
        self.connection = connection

    def save_all(self, candles: Sequence[Candle]) -> int:
        if not candles:
            return 0

        rows = [
            (
                candle.exchange,
                candle.symbol,
                candle.interval,
                candle.open_time,
                candle.close_time,
                candle.open,
                candle.high,
                candle.low,
                candle.close,
                candle.volume,
            )
            for candle in candles
        ]

        query = """
        INSERT INTO candles (
            exchange,
            symbol,
            interval,
            open_time,
            close_time,
            open,
            high,
            low,
            close,
            volume
        )
        VALUES (
            %s, %s, %s, %s, %s, %s, %s, %s, %s, %s
        )
        ON CONFLICT (exchange, symbol, interval, open_time)
        DO UPDATE SET
            close_time = EXCLUDED.close_time,
            open = EXCLUDED.open,
            high = EXCLUDED.high,
            low = EXCLUDED.low,
            close = EXCLUDED.close,
            volume = EXCLUDED.volume,
            updated_at = NOW()
        """

        try:
            with self.connection.cursor() as cursor:
                cursor.executemany(query, rows)
            self.connection.commit()
            return len(rows)
        except psycopg.Error as exc:
            self.connection.rollback()
            raise RepositoryError("Failed to persist candles") from exc

    def find_by_market_range(
        self,
        *,
        exchange: str,
        symbol: str,
        interval: str,
        from_time: datetime,
        to_time: datetime,
    ) -> list[Candle]:
        query = """
        SELECT
            exchange,
            symbol,
            interval,
            open_time,
            close_time,
            open,
            high,
            low,
            close,
            volume
        FROM candles
        WHERE exchange = %s
          AND symbol = %s
          AND interval = %s
          AND open_time >= %s
          AND open_time < %s
        ORDER BY open_time ASC
        """

        try:
            with self.connection.cursor() as cursor:
                cursor.execute(query, (exchange, symbol, interval, from_time, to_time))
                rows = cursor.fetchall()
        except psycopg.Error as exc:
            self.connection.rollback()
            raise RepositoryError("Failed to load candles") from exc

        return [
            Candle(
                exchange=row[0],
                symbol=row[1],
                interval=row[2],
                open_time=row[3],
                close_time=row[4],
                open=row[5],
                high=row[6],
                low=row[7],
                close=row[8],
                volume=row[9],
            )
            for row in rows
        ]
