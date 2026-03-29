from datetime import UTC, datetime

from pydantic import BaseModel, ConfigDict, Field, field_serializer


class CandleImportRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    exchange: str
    symbol: str
    interval: str
    from_time: datetime = Field(alias="from", serialization_alias="from")
    to_time: datetime = Field(alias="to", serialization_alias="to")


class CandleImportResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    status: str
    exchange: str
    symbol: str
    interval: str
    imported: int
    from_time: datetime = Field(alias="from", serialization_alias="from")
    to_time: datetime = Field(alias="to", serialization_alias="to")

    @field_serializer("from_time", "to_time")
    def serialize_datetime(self, value: datetime) -> str:
        return value.astimezone(UTC).isoformat().replace("+00:00", "Z")
