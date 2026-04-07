from __future__ import annotations

from pathlib import Path

import pandas as pd

from backtesting.utils.validation import DataValidationError, validate_ohlcv_dataframe


class DataLoader:
    """Loads and validates OHLCV market data."""

    def __init__(self, *, strict: bool = True) -> None:
        self.strict = strict

    def load_csv(self, file_path: str | Path) -> pd.DataFrame:
        resolved_path = Path(file_path).expanduser().resolve(strict=True)

        dataframe = pd.read_csv(resolved_path)
        if "timestamp" not in dataframe.columns:
            raise DataValidationError("Missing required columns: timestamp")

        dataframe["timestamp"] = pd.to_datetime(
            dataframe["timestamp"],
            utc=True,
            errors="coerce",
        )

        return validate_ohlcv_dataframe(dataframe, strict=self.strict)

