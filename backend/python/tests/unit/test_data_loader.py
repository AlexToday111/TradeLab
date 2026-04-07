from __future__ import annotations

from pathlib import Path

import pandas as pd
import pytest

from backtesting.data.data_loader import DataLoader
from backtesting.utils.validation import DataValidationError


def write_csv(tmp_path: Path, name: str, dataframe: pd.DataFrame) -> Path:
    path = tmp_path / name
    dataframe.to_csv(path, index=False)
    return path


def test_load_csv_rejects_unsorted_data_in_strict_mode(tmp_path):
    path = write_csv(
        tmp_path,
        "candles.csv",
        pd.DataFrame(
            [
                {
                    "timestamp": "2024-01-02T00:00:00Z",
                    "open": 11,
                    "high": 12,
                    "low": 10,
                    "close": 11.5,
                    "volume": 100,
                },
                {
                    "timestamp": "2024-01-01T00:00:00Z",
                    "open": 10,
                    "high": 11,
                    "low": 9,
                    "close": 10.5,
                    "volume": 90,
                },
            ]
        ),
    )

    with pytest.raises(DataValidationError, match="sorted by timestamp"):
        DataLoader(strict=True).load_csv(path)


def test_load_csv_normalizes_non_strict_data(tmp_path):
    path = write_csv(
        tmp_path,
        "candles.csv",
        pd.DataFrame(
            [
                {
                    "timestamp": "2024-01-02T00:00:00Z",
                    "open": 11,
                    "high": 12,
                    "low": 10,
                    "close": 11.5,
                    "volume": 100,
                },
                {
                    "timestamp": "2024-01-01T00:00:00Z",
                    "open": 10,
                    "high": 11,
                    "low": 9,
                    "close": 10.5,
                    "volume": 90,
                },
                {
                    "timestamp": "2024-01-02T00:00:00Z",
                    "open": 12,
                    "high": 13,
                    "low": 11,
                    "close": 12.5,
                    "volume": 110,
                },
                {
                    "timestamp": "2024-01-03T00:00:00Z",
                    "open": "bad",
                    "high": 14,
                    "low": 12,
                    "close": 13.5,
                    "volume": 120,
                },
            ]
        ),
    )

    dataframe = DataLoader(strict=False).load_csv(path)

    assert dataframe["timestamp"].dt.strftime("%Y-%m-%dT%H:%M:%SZ").tolist() == [
        "2024-01-01T00:00:00Z",
        "2024-01-02T00:00:00Z",
    ]
    assert dataframe["open"].tolist() == [10.0, 11.0]


def test_load_csv_rejects_nan_in_ohlc_columns(tmp_path):
    path = write_csv(
        tmp_path,
        "candles.csv",
        pd.DataFrame(
            [
                {
                    "timestamp": "2024-01-01T00:00:00Z",
                    "open": None,
                    "high": 11,
                    "low": 9,
                    "close": 10.5,
                    "volume": 90,
                }
            ]
        ),
    )

    with pytest.raises(DataValidationError, match="NaN values detected in OHLC columns"):
        DataLoader(strict=True).load_csv(path)

