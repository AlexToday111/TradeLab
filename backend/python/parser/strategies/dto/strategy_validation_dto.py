from typing import Any

from pydantic import BaseModel, ConfigDict, Field


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
