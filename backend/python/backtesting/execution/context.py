from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True, slots=True)
class ExecutionContext:
    strategy_path: str
    data_path: str
    output_dir: str
    run_id: str | None = None
    correlation_id: str | None = None

    @property
    def output_path(self) -> Path:
        return Path(self.output_dir)
