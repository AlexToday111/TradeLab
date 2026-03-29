import time
from datetime import datetime
from typing import Any

import requests

from parser.common.config.settings import settings
from parser.common.exceptions import ExchangeError
from parser.imports.exchanges.base import BaseExchangeClient


class BinanceClient(BaseExchangeClient):
    def __init__(self) -> None:
        self.base_url = settings.binance_base_url.rstrip("/")
        self.timeout_seconds = settings.request_timeout_seconds
        self.per_request_limit = settings.binance_klines_page_limit
        self.max_retries = settings.binance_max_retries
        self.retry_backoff_seconds = settings.binance_retry_backoff_seconds
        self.session = requests.Session()
        if settings.binance_api_key:
            self.session.headers.update({"X-MBX-APIKEY": settings.binance_api_key})

    def _fetch_klines_page(self, params: dict[str, Any]) -> list[Any]:
        url = f"{self.base_url}/api/v3/klines"

        for attempt in range(self.max_retries + 1):
            try:
                response = self.session.get(url, params=params, timeout=self.timeout_seconds)
                response.raise_for_status()
                payload = response.json()
                if not isinstance(payload, list):
                    raise ExchangeError("Unexpected Binance response format")
                return payload
            except ExchangeError:
                raise
            except requests.exceptions.HTTPError as exc:
                status = exc.response.status_code if exc.response else None
                if status not in {429, 500, 502, 503, 504} or attempt >= self.max_retries:
                    raise ExchangeError("Binance returned an error response") from exc
            except requests.exceptions.RequestException as exc:
                if attempt >= self.max_retries:
                    raise ExchangeError("Failed to request candles from Binance") from exc

            time.sleep(self.retry_backoff_seconds * (attempt + 1))

        return []

    def load_klines_raw(
        self,
        symbol: str,
        interval: str,
        start_time: datetime,
        end_time: datetime,
    ) -> list[Any]:
        all_klines: list[Any] = []
        next_start_ms = int(start_time.timestamp() * 1000)
        end_time_ms = int(end_time.timestamp() * 1000)

        while next_start_ms < end_time_ms:
            params = {
                "symbol": symbol,
                "interval": interval,
                "limit": self.per_request_limit,
                "startTime": next_start_ms,
                "endTime": end_time_ms,
            }
            klines = self._fetch_klines_page(params)
            if not klines:
                break

            all_klines.extend(klines)
            last_open_time = int(klines[-1][0])
            if last_open_time >= end_time_ms:
                break

            next_start_ms = last_open_time + 1

        return all_klines
