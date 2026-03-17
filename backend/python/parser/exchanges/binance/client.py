import time
from datetime import datetime
from typing import Any

import requests

from parser.config import settings
from parser.exchanges.base import BaseExchangeClient


class BinanceClient(BaseExchangeClient):
    def __init__(self) -> None:
        self.base_url = settings.binance_base_url.rstrip("/")
        self.timeout_seconds = settings.request_timeout_seconds
        self.sleep_between_requests = settings.sleep_between_requests_seconds
        self.per_request_limit = settings.binance_klines_page_limit
        self.max_retries = settings.binance_max_retries
        self.retry_backoff_seconds = settings.binance_retry_backoff_seconds
        self.session = requests.Session()

    def _fetch_klines_page(self, url: str, params: dict[str, Any]) -> list[Any]:
        for attempt in range(self.max_retries + 1):
            try:
                response = self.session.get(url, params=params, timeout=self.timeout_seconds)
                response.raise_for_status()
                return response.json()
            except requests.exceptions.HTTPError as exc:
                status = exc.response.status_code if exc.response else None
                if status not in {429, 500, 502, 503, 504} or attempt >= self.max_retries:
                    raise
            except requests.exceptions.RequestException:
                if attempt >= self.max_retries:
                    raise

            time.sleep(self.retry_backoff_seconds * (attempt + 1))

        return []

    def load_klines_raw(
        self,
        symbol: str,
        interval: str,
        limit_total: int | None = None,
        start_time: datetime | None = None,
        end_time: datetime | None = None,
    ) -> list[Any]:

        url = f"{self.base_url}/api/v3/klines"
        all_klines: list[Any] = []

        if start_time or end_time:
            start_time_ms = int(start_time.timestamp() * 1000) if start_time else None
            end_time_ms = int(end_time.timestamp() * 1000) if end_time else None
            next_start_ms = start_time_ms

            while True:
                current_limit = self.per_request_limit
                if limit_total is not None:
                    remaining = limit_total - len(all_klines)
                    if remaining <= 0:
                        break
                    current_limit = min(current_limit, remaining)

                params = {
                    "symbol": symbol,
                    "interval": interval,
                    "limit": current_limit,
                }

                if next_start_ms is not None:
                    params["startTime"] = next_start_ms
                if end_time_ms is not None:
                    params["endTime"] = end_time_ms

                klines = self._fetch_klines_page(url, params)

                if not klines:
                    break

                all_klines.extend(klines)
                next_start_ms = klines[-1][0] + 1

                if end_time_ms is not None and next_start_ms >= end_time_ms:
                    break

                time.sleep(self.sleep_between_requests)
        else:
            if limit_total is None:
                raise ValueError("limit_total is required when start_time/end_time are not provided")

            end_time_ms: int | None = None
            while len(all_klines) < limit_total:
                current_limit = min(self.per_request_limit, limit_total - len(all_klines))

                params = {
                    "symbol": symbol,
                    "interval": interval,
                    "limit": current_limit,
                }

                if end_time_ms is not None:
                    params["endTime"] = end_time_ms - 1

                klines = self._fetch_klines_page(url, params)

                if not klines:
                    break

                all_klines = klines + all_klines
                end_time_ms = klines[0][0]

                time.sleep(self.sleep_between_requests)

        if limit_total is None:
            return all_klines

        return all_klines[-limit_total:]
