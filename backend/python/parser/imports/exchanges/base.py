from abc import ABC, abstractmethod
from datetime import datetime
from typing import Any


class BaseExchangeClient(ABC):
    @abstractmethod
    def load_klines_raw(
        self,
        symbol: str,
        interval: str,
        start_time: datetime,
        end_time: datetime,
    ) -> list[Any]:
        raise NotImplementedError
