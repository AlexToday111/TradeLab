from __future__ import annotations

from dataclasses import dataclass

import pandas as pd

REQUIRED_COLUMNS: tuple[str, ...] = (
    "timestamp",
    "open",
    "high",
    "low",
    "close",
    "volume",
)


class DataValidationError(ValueError):
    """Raised when market data fails validation."""


@dataclass(frozen=True, slots=True)
class ValidationConfig:
    strict: bool = True


def validate_ohlcv_dataframe(
    dataframe: pd.DataFrame,
    *,
    strict: bool = True,
) -> pd.DataFrame:
    missing_columns = [column for column in REQUIRED_COLUMNS if column not in dataframe.columns]
    if missing_columns:
        missing = ", ".join(missing_columns)
        raise DataValidationError(f"Missing required columns: {missing}")

    normalized = dataframe.loc[:, list(REQUIRED_COLUMNS)].copy()

    if strict and not normalized["timestamp"].is_monotonic_increasing:
        raise DataValidationError("Data must be sorted by timestamp in strict mode")

    if not strict:
        normalized = normalized.sort_values("timestamp", kind="mergesort")

    duplicates = normalized["timestamp"].duplicated(keep="first")
    if duplicates.any():
        if strict:
            raise DataValidationError("Duplicate timestamps detected")
        normalized = normalized.loc[~duplicates]

    ohlc_nulls = normalized.loc[:, ["open", "high", "low", "close"]].isna().any(axis=1)
    if ohlc_nulls.any():
        if strict:
            raise DataValidationError("NaN values detected in OHLC columns")
        normalized = normalized.loc[~ohlc_nulls]

    required_nulls = normalized.loc[:, list(REQUIRED_COLUMNS)].isna().any(axis=1)
    if required_nulls.any():
        if strict:
            raise DataValidationError("Missing values detected in required fields")
        normalized = normalized.loc[~required_nulls]

    return normalized.reset_index(drop=True)
