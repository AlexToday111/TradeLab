import argparse

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
        required=True,
        type=int,
        help="Number of candles to load",
    )

    return parser


def main() -> None:
    parser = build_parser()
    args = parser.parse_args()

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
        )

        print(
            f"Imported candles successfully: "
            f"exchange={args.exchange}, symbol={args.symbol}, "
            f"interval={args.interval}, count={inserted_count}"
        )
    finally:
        connection.close()


if __name__ == "__main__":
    main()