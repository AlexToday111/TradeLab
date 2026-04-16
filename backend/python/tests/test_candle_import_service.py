from datetime import UTC, datetime
from unittest.mock import Mock

import pytest

from parser.candles.models.candle import Candle
from parser.common.exceptions import ValidationError
from parser.imports.dto.candle_import_dto import CandleImportRequest
from parser.imports.services.candle_import_service import CandleImportService


def build_request(**overrides) -> CandleImportRequest:
    payload = {
        "exchange": " Binance ",
        "symbol": " btcusdt ",
        "interval": "1h",
        "from": datetime(2024, 1, 1, 0, 0, 0),
        "to": datetime(2024, 1, 1, 1, 0, 0, tzinfo=UTC),
    }
    payload.update(overrides)
    return CandleImportRequest(**payload)


def test_import_candles_normalizes_payload_and_persists_mapped_candles(monkeypatch):
    repository = Mock()
    repository.save_all.return_value = 2

    client = Mock()
    client.load_klines_raw.return_value = [["raw-1"], ["raw-2"]]

    candles = [
        Candle(
            exchange="binance",
            symbol="BTCUSDT",
            interval="1h",
            open_time=datetime(2024, 1, 1, 0, 0, 0, tzinfo=UTC),
            close_time=datetime(2024, 1, 1, 1, 0, 0, tzinfo=UTC),
            open=1,
            high=2,
            low=0.5,
            close=1.5,
            volume=10,
        ),
        Candle(
            exchange="binance",
            symbol="BTCUSDT",
            interval="1h",
            open_time=datetime(2024, 1, 1, 1, 0, 0, tzinfo=UTC),
            close_time=datetime(2024, 1, 1, 2, 0, 0, tzinfo=UTC),
            open=1.5,
            high=2.5,
            low=1,
            close=2,
            volume=12,
        ),
    ]

    monkeypatch.setattr(
        "parser.imports.services.candle_import_service.get_exchange_client",
        lambda exchange: client,
    )
    monkeypatch.setattr(
        "parser.imports.services.candle_import_service.map_binance_klines",
        lambda symbol, interval, raw_klines: candles,
    )

    service = CandleImportService(repository)
    response = service.import_candles(build_request())

    client.load_klines_raw.assert_called_once()
    repository.save_all.assert_called_once_with(candles)

    assert response.status == "success"
    assert response.exchange == "binance"
    assert response.symbol == "BTCUSDT"
    assert response.imported == 2
    assert response.dataset["source"] == "binance"
    assert response.dataset["rowsCount"] == 2
    assert response.dataset["qualityFlags"] == []
    assert response.dataset["datasetId"].startswith("dataset-binance-btcusdt-1h-")


def test_import_candles_rejects_invalid_time_range():
    repository = Mock()
    service = CandleImportService(repository)

    with pytest.raises(ValidationError, match="'from' must be earlier than 'to'"):
        service.import_candles(
            build_request(
                **{
                    "from": datetime(2024, 1, 1, 1, 0, 0, tzinfo=UTC),
                    "to": datetime(2024, 1, 1, 1, 0, 0, tzinfo=UTC),
                }
            )
        )
