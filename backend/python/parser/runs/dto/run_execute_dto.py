from typing import Any

from pydantic import BaseModel, ConfigDict, Field


class RunExecuteRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    strategy_file_path: str = Field(
        alias="strategyFilePath",
        serialization_alias="strategyFilePath",
        description="Absolute or service-local path to the strategy file.",
        examples=["/app/strategies/ema_cross.py"],
    )
    exchange: str = Field(description="Exchange code.", examples=["binance"])
    symbol: str = Field(description="Trading pair symbol.", examples=["BTCUSDT"])
    interval: str = Field(description="Candle interval.", examples=["1h"])
    from_time: str = Field(
        alias="from",
        serialization_alias="from",
        description="Execution range start in ISO-8601 UTC format.",
        examples=["2024-01-01T00:00:00Z"],
    )
    to_time: str = Field(
        alias="to",
        serialization_alias="to",
        description="Execution range end in ISO-8601 UTC format.",
        examples=["2024-01-31T23:59:59Z"],
    )
    params: dict[str, Any] = Field(
        default_factory=dict,
        description="Strategy runtime parameters.",
        examples=[{"fast_period": 9, "slow_period": 21}],
    )


class RunExecuteResponse(BaseModel):
    success: bool = Field(description="Whether execution completed successfully.", examples=[True])
    metrics: dict[str, Any] | None = Field(
        default=None,
        description="Strategy execution metrics payload.",
        examples=[{"total_return": 0.124, "max_drawdown": 0.038}],
    )
    error: str | None = Field(default=None, description="Execution error message.", examples=[None])
