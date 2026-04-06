from typing import Any

from pydantic import BaseModel, ConfigDict, Field


class StrategyValidationRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    file_path: str = Field(
        alias="filePath",
        serialization_alias="filePath",
        description="Absolute or service-local path to the strategy file.",
        examples=["/app/strategies/ema_cross.py"],
    )


class StrategyValidationResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    valid: bool = Field(description="Whether the strategy file passed validation.", examples=[True])
    name: str | None = Field(
        default=None,
        description="Resolved strategy name.",
        examples=["EMA Cross"],
    )
    parameters_schema: dict[str, Any] | None = Field(
        alias="parametersSchema",
        serialization_alias="parametersSchema",
        default=None,
        description="JSON schema of strategy parameters.",
        examples=[{"type": "object", "properties": {"fast_period": {"type": "integer"}}}],
    )
    error: str | None = Field(
        default=None,
        description="Validation error message.",
        examples=[None],
    )
