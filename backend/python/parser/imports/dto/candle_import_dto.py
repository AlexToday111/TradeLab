from hashlib import sha256
from datetime import UTC, datetime

from pydantic import BaseModel, ConfigDict, Field, field_serializer


class CandleImportRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    exchange: str = Field(description="Exchange code.", examples=["binance"])
    symbol: str = Field(description="Trading pair symbol.", examples=["BTCUSDT"])
    interval: str = Field(description="Candle interval.", examples=["1h"])
    from_time: datetime = Field(
        alias="from",
        serialization_alias="from",
        description="Inclusive range start in UTC.",
        examples=["2024-01-01T00:00:00Z"],
    )
    to_time: datetime = Field(
        alias="to",
        serialization_alias="to",
        description="Inclusive range end in UTC.",
        examples=["2024-01-10T00:00:00Z"],
    )


class CandleImportResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    status: str = Field(description="Import execution status.", examples=["success"])
    exchange: str = Field(description="Exchange code.", examples=["binance"])
    symbol: str = Field(description="Trading pair symbol.", examples=["BTCUSDT"])
    interval: str = Field(description="Candle interval.", examples=["1h"])
    imported: int = Field(description="Number of imported candles.", examples=[240])
    from_time: datetime = Field(
        alias="from",
        serialization_alias="from",
        description="Imported range start in UTC.",
        examples=["2024-01-01T00:00:00Z"],
    )
    to_time: datetime = Field(
        alias="to",
        serialization_alias="to",
        description="Imported range end in UTC.",
        examples=["2024-01-10T00:00:00Z"],
    )
    dataset: dict[str, object] = Field(
        description="Dataset metadata produced by the import.",
        examples=[
            {
                "datasetId": "dataset-binance-btcusdt-1h-a1b2c3d4e5f6",
                "source": "binance",
                "symbol": "BTCUSDT",
                "timeframe": "1h",
                "importedAt": "2024-01-10T00:00:00Z",
                "rowsCount": 240,
                "startAt": "2024-01-01T00:00:00Z",
                "endAt": "2024-01-10T00:00:00Z",
                "version": "a1b2c3d4e5f6",
                "fingerprint": "a1b2c3d4e5f6",
                "qualityFlags": [],
                "lineage": {"importRange": {"from": "2024-01-01T00:00:00Z", "to": "2024-01-10T00:00:00Z"}},
            }
        ],
    )

    @field_serializer("from_time", "to_time")
    def serialize_datetime(self, value: datetime) -> str:
        return value.astimezone(UTC).isoformat().replace("+00:00", "Z")


def make_dataset_id(exchange: str, symbol: str, interval: str, fingerprint: str) -> str:
    normalized = f"{exchange.strip().lower()}-{symbol.strip().lower()}-{interval.strip().lower()}"
    return f"dataset-{normalized}-{fingerprint[:12]}"


def make_dataset_fingerprint(parts: list[str]) -> str:
    payload = "|".join(parts).encode("utf-8")
    return sha256(payload).hexdigest()[:12]
