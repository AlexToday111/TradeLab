from typing import Any

from pydantic import BaseModel, ConfigDict, Field


class RunExecuteRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    strategy_file_path: str = Field(alias="strategyFilePath", serialization_alias="strategyFilePath")
    exchange: str
    symbol: str
    interval: str
    from_time: str = Field(alias="from", serialization_alias="from")
    to_time: str = Field(alias="to", serialization_alias="to")
    params: dict[str, Any] = Field(default_factory=dict)


class RunExecuteResponse(BaseModel):
    success: bool
    metrics: dict[str, Any] | None
    error: str | None
