import os
from dataclasses import dataclass

from dotenv import load_dotenv

load_dotenv()


@dataclass(frozen=True)
class Settings:
    database_host: str
    database_port: int
    database_name: str
    database_user: str
    database_password: str

    binance_base_url: str
    request_timeout_seconds: int
    sleep_between_requests_seconds: float
    binance_klines_page_limit: int
    binance_max_retries: int
    binance_retry_backoff_seconds: float

    @property
    def database_dsn(self) -> str:
        return (
            f"host={self.database_host} "
            f"port={self.database_port} "
            f"dbname={self.database_name} "
            f"user={self.database_user} "
            f"password={self.database_password}"
        )


settings = Settings(
    database_host=os.getenv("DB_HOST", "localhost"),
    database_port=int(os.getenv("DB_PORT", "5432")),
    database_name=os.getenv("DB_NAME", "tradelab"),
    database_user=os.getenv("DB_USER", "postgres"),
    database_password=os.getenv("DB_PASSWORD", "postgres"),
    binance_base_url=os.getenv("BINANCE_BASE_URL", "https://api.binance.com"),
    request_timeout_seconds=int(os.getenv("REQUEST_TIMEOUT_SECONDS", "15")),
    sleep_between_requests_seconds=float(os.getenv("SLEEP_BETWEEN_REQUESTS_SECONDS", "0.1")),
    binance_klines_page_limit=int(os.getenv("BINANCE_KLINES_PAGE_LIMIT", "1000")),
    binance_max_retries=int(os.getenv("BINANCE_MAX_RETRIES", "3")),
    binance_retry_backoff_seconds=float(os.getenv("BINANCE_RETRY_BACKOFF_SECONDS", "1.0")),
)
