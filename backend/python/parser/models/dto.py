from datetime import UTC, datetime
from typing import Any

from pydantic import BaseModel, ConfigDict, Field, field_serializer


class HealthResponse(BaseModel):
    status: str
    service: str


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


class StrategyValidationRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    file_path: str = Field(alias="filePath", serialization_alias="filePath")


class StrategyValidationResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    valid: bool
    name: str | None
    parameters_schema: dict[str, Any] | None = Field(
        alias="parametersSchema",
        serialization_alias="parametersSchema",
    )
    error: str | None
