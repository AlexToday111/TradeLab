from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
from typing import Protocol

import pandas as pd

from backtesting.data.data_loader import DataLoader


@dataclass(frozen=True, slots=True)
class DatasetReference:
    path: str
    row_count: int
    start_at: str | None
    end_at: str | None

    def to_dict(self) -> dict[str, object]:
        return {
            "path": self.path,
            "rowCount": self.row_count,
            "startAt": self.start_at,
            "endAt": self.end_at,
        }


class DataProvider(Protocol):
    def load(self) -> tuple[pd.DataFrame, DatasetReference]:
        ...


class CsvDataProvider:
    def __init__(self, file_path: str | Path, *, strict: bool = True) -> None:
        self._resolved_path = Path(file_path).expanduser().resolve(strict=True)
        self._loader = DataLoader(strict=strict)

    def load(self) -> tuple[pd.DataFrame, DatasetReference]:
        dataframe = self._loader.load_csv(self._resolved_path)
        start_at = None
        end_at = None
        if not dataframe.empty:
            start_at = dataframe["timestamp"].iloc[0].isoformat()
            end_at = dataframe["timestamp"].iloc[-1].isoformat()

        reference = DatasetReference(
            path=str(self._resolved_path),
            row_count=len(dataframe.index),
            start_at=start_at,
            end_at=end_at,
        )
        return dataframe, reference
