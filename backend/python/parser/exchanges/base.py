from abc import ABC, abstractmethod
from datetime import datetime
from typing import Any

class BaseExchangeClient(ABC):
    @abstractmethod
    def load_klines_raw(
            self,
            symbol: str,
            interval: str,
            limit_total: int | None = None,
            start_time: datetime | None = None,
            end_time: datetime | None = None,
    ) -> list[Any]:
        raise NotImplementedError()
