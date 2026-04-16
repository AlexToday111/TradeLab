from __future__ import annotations

import json
from pathlib import Path
from typing import Any, Protocol

from backtesting.execution.context import ExecutionContext
from backtesting.models.result import BacktestResult


class ArtifactWriter(Protocol):
    def write(self, *, context: ExecutionContext, result: BacktestResult) -> dict[str, object]:
        ...


class JsonArtifactWriter:
    def write(self, *, context: ExecutionContext, result: BacktestResult) -> dict[str, object]:
        output_dir = context.output_path
        output_dir.mkdir(parents=True, exist_ok=True)

        trades_payload = [trade.to_dict() for trade in result.trades]
        trades_path = self._write_file(output_dir / "trades.json", trades_payload)
        equity_path = self._write_file(
            output_dir / "equity_curve.json",
            [point.to_dict() for point in result.equity_curve],
        )
        summary_path = self._write_file(output_dir / "summary.json", result.summary)
        logs_path = self._write_file(output_dir / "logs.json", result.logs)
        warnings_path = self._write_file(output_dir / "warnings.json", result.warnings)

        return {
            "outputDir": str(output_dir),
            "tradesPath": trades_path,
            "equityCurvePath": equity_path,
            "summaryPath": summary_path,
            "logsPath": logs_path,
            "warningsPath": warnings_path,
            "tradesCount": len(result.trades),
            "equityPointCount": len(result.equity_curve),
        }

    def _write_file(self, path: Path, payload: Any) -> str:
        path.write_text(
            json.dumps(self._make_json_safe(payload), ensure_ascii=True) + "\n",
            encoding="utf-8",
        )
        return str(path)

    def _make_json_safe(self, value: Any) -> Any:
        if isinstance(value, dict):
            return {key: self._make_json_safe(item) for key, item in value.items()}
        if isinstance(value, list):
            return [self._make_json_safe(item) for item in value]
        if isinstance(value, float):
            return round(value, 4)
        return value
