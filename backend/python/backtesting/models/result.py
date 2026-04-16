from __future__ import annotations

from dataclasses import asdict, dataclass, field
from datetime import datetime

from backtesting.models.trade import Trade


@dataclass(frozen=True, slots=True)
class EquityPoint:
    timestamp: datetime
    cash: float
    equity: float
    position_size: float

    def to_dict(self) -> dict[str, object]:
        payload = asdict(self)
        payload["timestamp"] = self.timestamp.isoformat()
        return payload


@dataclass(frozen=True, slots=True)
class BacktestResult:
    summary: dict[str, float | int]
    trades: list[Trade]
    equity_curve: list[EquityPoint]
    logs: list[str]
    warnings: list[str]
    metadata: dict[str, object] = field(default_factory=dict)

    def to_dict(self) -> dict[str, object]:
        return {
            "summary": self.summary,
            "trades": [trade.to_dict() for trade in self.trades],
            "equity_curve": [point.to_dict() for point in self.equity_curve],
            "logs": self.logs,
            "warnings": self.warnings,
            "metadata": self.metadata,
        }
