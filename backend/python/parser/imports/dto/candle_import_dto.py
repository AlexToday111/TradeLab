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

    @field_serializer("from_time", "to_time")
    def serialize_datetime(self, value: datetime) -> str:
        return value.astimezone(UTC).isoformat().replace("+00:00", "Z")
