import os
from dataclasses import dataclass

from dotenv import load_dotenv

load_dotenv()


@dataclass(frozen=True)
class Settings:
    db_host: str
    db_port: int
    db_name: str
    db_user: str
    db_password: str
    binance_base_url: str
    binance_api_key: str
    binance_api_secret: str
    python_service_port: int
    request_timeout_seconds: int
    binance_klines_page_limit: int
    binance_max_retries: int
    binance_retry_backoff_seconds: float

    @property
    def database_dsn(self) -> str:
        return (
            f"host={self.db_host} "
            f"port={self.db_port} "
            f"dbname={self.db_name} "
            f"user={self.db_user} "
            f"password={self.db_password}"
        )


def load_settings() -> Settings:
    return Settings(
        db_host=os.getenv("DB_HOST", "localhost"),
        db_port=int(os.getenv("DB_PORT", "5432")),
        db_name=os.getenv("DB_NAME", "tradelab"),
        db_user=os.getenv("DB_USER", "postgres"),
        db_password=os.getenv("DB_PASSWORD", "postgres"),
        binance_base_url=os.getenv("BINANCE_BASE_URL", "https://api.binance.com"),
        binance_api_key=os.getenv("BINANCE_API_KEY", ""),
        binance_api_secret=os.getenv("BINANCE_API_SECRET", ""),
        python_service_port=int(os.getenv("PYTHON_SERVICE_PORT", "8000")),
        request_timeout_seconds=int(os.getenv("REQUEST_TIMEOUT_SECONDS", "15")),
        binance_klines_page_limit=int(os.getenv("BINANCE_KLINES_PAGE_LIMIT", "1000")),
        binance_max_retries=int(os.getenv("BINANCE_MAX_RETRIES", "3")),
        binance_retry_backoff_seconds=float(os.getenv("BINANCE_RETRY_BACKOFF_SECONDS", "1.0")),
    )


settings = load_settings()
