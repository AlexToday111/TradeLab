from typing import Sequence

from psycopg2.extras import execute_values

from parser.models.candle import Candle


class CandleRepository:
    def __init__(self, connection) -> None:
        self.connection = connection

    def create_table_if_not_exists(self) -> None:
        query = """
        CREATE TABLE IF NOT EXISTS market_candles (
            id BIGSERIAL PRIMARY KEY,
            exchange VARCHAR(32) NOT NULL,
            symbol VARCHAR(32) NOT NULL,
            interval VARCHAR(16) NOT NULL,
            open_time TIMESTAMPTZ NOT NULL,
            open NUMERIC(20, 8) NOT NULL,
            high NUMERIC(20, 8) NOT NULL,
            low NUMERIC(20, 8) NOT NULL,
            close NUMERIC(20, 8) NOT NULL,
            volume NUMERIC(28, 8) NOT NULL,
            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
            UNIQUE (exchange, symbol, interval, open_time)
        );
        """

        with self.connection.cursor() as cursor:
            cursor.execute(query)

        self.connection.commit()

    def save_all(self, candles: Sequence[Candle]) -> int:
        if not candles:
            return 0

        rows = [
            (
                candle.exchange,
                candle.symbol,
                candle.interval,
                candle.open_time,
                candle.open,
                candle.high,
                candle.low,
                candle.close,
                candle.volume,
            )
            for candle in candles
        ]

        query = """
        INSERT INTO market_candles (
            exchange,
            symbol,
            interval,
            open_time,
            open,
            high,
            low,
            close,
            volume
        )
        VALUES %s
        ON CONFLICT (exchange, symbol, interval, open_time)
        DO UPDATE SET
            open = EXCLUDED.open,
            high = EXCLUDED.high,
            low = EXCLUDED.low,
            close = EXCLUDED.close,
            volume = EXCLUDED.volume
        """

        with self.connection.cursor() as cursor:
            execute_values(cursor, query, rows)

        self.connection.commit()
        return len(rows)