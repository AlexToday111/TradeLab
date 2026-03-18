import argparse
import logging
from datetime import datetime, timezone

from parser.db import get_connection
from parser.repositories.candle_repository import CandleRepository
from parser.services.candle_import_service import CandleImportService


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="TradeLab candles importer")

    parser.add_argument(
        "--exchange",
        required=True,
        help="Exchange name, e.g. binance",
    )
    parser.add_argument(
        "--symbol",
        required=True,
        help="Trading pair, e.g. BTCUSDT",
    )
    parser.add_argument(
        "--interval",
        required=True,
        help="Candle interval, e.g. 5m",
    )
    parser.add_argument(
        "--limit",
        type=int,
        help="Number of candles to load (mutually exclusive with --start/--end)",
    )
    parser.add_argument(
        "--start",
        help="Start datetime (ISO 8601), e.g. 2024-01-01T00:00:00Z",
    )
    parser.add_argument(
        "--end",
        help="End datetime (ISO 8601), e.g. 2024-01-02T00:00:00Z",
    )

    return parser


def parse_datetime(value: str) -> datetime:
    normalized = value.replace("Z", "+00:00")
    parsed = datetime.fromisoformat(normalized)
    if parsed.tzinfo is None:
        parsed = parsed.replace(tzinfo=timezone.utc)
    return parsed.astimezone(timezone.utc)


def main() -> None:
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s %(levelname)s %(name)s: %(message)s",
    )
    logger = logging.getLogger(__name__)

    parser = build_parser()
    args = parser.parse_args()

    if args.limit is None:
        if not args.start or not args.end:
            parser.error("Either --limit or both --start/--end must be provided")
    else:
        if args.start or args.end:
            parser.error("--limit is mutually exclusive with --start/--end")

    start_time = parse_datetime(args.start) if args.start else None
    end_time = parse_datetime(args.end) if args.end else None
    if start_time and end_time and start_time >= end_time:
        parser.error("--start must be earlier than --end")

    connection = get_connection()
    try:
        candle_repository = CandleRepository(connection)
        candle_repository.create_table_if_not_exists()

        candle_import_service = CandleImportService(candle_repository)

        inserted_count = candle_import_service.import_candles(
            exchange=args.exchange,
            symbol=args.symbol,
            interval=args.interval,
            limit_total=args.limit,
            start_time=start_time,
            end_time=end_time,
        )

        logger.info(
            "Imported candles successfully: exchange=%s, symbol=%s, interval=%s, count=%s",
            args.exchange,
            args.symbol,
            args.interval,
            inserted_count,
        )
    finally:
        connection.close()


if __name__ == "__main__":
    main()
