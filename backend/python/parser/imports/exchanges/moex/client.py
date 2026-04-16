import time
from datetime import datetime
from typing import Any

import requests

from parser.common.config.settings import settings
from parser.common.exceptions import ExchangeError
from parser.imports.exchanges.base import BaseExchangeClient


class MoexClient(BaseExchangeClient):
    def __init__(self) -> None:
        self.base_url = settings.moex_base_url.rstrip("/")
        self.timeout_seconds = settings.request_timeout_seconds
        self.max_retries = settings.binance_max_retries
        self.retry_backoff_seconds = settings.binance_retry_backoff_seconds
        self.session = requests.Session()

    def _fetch_klines_page(self, url: str, params: dict[str, Any]) -> tuple[list[str], list[Any]]:
        for attempt in range(self.max_retries + 1):
            try:
                response = self.session.get(url, params=params, timeout=self.timeout_seconds)
                response.raise_for_status()
                payload = response.json()
                candles = payload.get("candles", {})
                columns = candles.get("columns", [])
                rows = candles.get("data", [])
                if not isinstance(columns, list) or not isinstance(rows, list):
                    raise ExchangeError("Unexpected MOEX response format")
                return columns, rows
            except ExchangeError:
                raise
            except requests.exceptions.HTTPError as exc:
                status = exc.response.status_code if exc.response else None
                if status not in {429, 500, 502, 503, 504} or attempt >= self.max_retries:
                    raise ExchangeError("MOEX returned an error response") from exc
            except requests.exceptions.RequestException as exc:
                if attempt >= self.max_retries:
                    raise ExchangeError("Failed to request candles from MOEX") from exc

            time.sleep(self.retry_backoff_seconds * (attempt + 1))

        return [], []

    def load_klines_raw(
        self,
        symbol: str,
        interval: str,
        start_time: datetime,
        end_time: datetime,
        **kwargs: Any,
    ) -> list[Any]:
        engine = str(kwargs.get("engine") or "stock").strip().lower()
        market = str(kwargs.get("market") or "shares").strip().lower()
        board = kwargs.get("board")
        api_interval = _map_interval_to_moex(interval)
        path_parts = [self.base_url, "engines", engine, "markets", market]
        if board:
            path_parts.extend(["boards", str(board).strip().upper()])
        path_parts.extend(["securities", symbol, "candles.json"])
        url = "/".join(str(part).strip("/") for part in path_parts)

        all_rows: list[Any] = []
        start = 0

        while True:
            params = {
                "from": start_time.date().isoformat(),
                "till": end_time.date().isoformat(),
                "interval": api_interval,
                "start": start,
            }

            columns, rows = self._fetch_klines_page(url, params)
            if not rows:
                break

            if not all_rows:
                all_rows.append(columns)
            all_rows.extend(rows)
            start += len(rows)

        return all_rows


def _map_interval_to_moex(interval: str) -> int:
    normalized = interval.strip().lower()
    mapping = {
        "1m": 1,
        "10m": 10,
        "1h": 60,
        "1d": 24,
        "1w": 7,
        "1mo": 31,
        "1mth": 31,
    }
    if normalized not in mapping:
        raise ExchangeError(f"Unsupported MOEX interval: {interval}")
    return mapping[normalized]
