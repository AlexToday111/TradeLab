from typing import Sequence

import psycopg

from parser.candles.models.candle import Candle
from parser.common.exceptions import RepositoryError


class CandleImportRepository:
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
