from __future__ import annotations

import json
import sys
import tempfile
from pathlib import Path
from typing import Any

from backtesting.engine.backtest_engine import BacktestConfig, BacktestEngine


def execute_run(payload: dict[str, Any]) -> dict[str, Any]:
    data_path = payload.get("data_path")
    strategy_path = payload.get("strategy_path")
    if not data_path or not strategy_path:
        raise ValueError("Missing required fields: data_path and strategy_path")

    config = BacktestConfig(
        initial_cash=float(payload.get("initial_cash", 10_000.0)),
        fee_rate=float(payload.get("fee_rate", 0.0)),
        slippage_bps=float(payload.get("slippage_bps", 0.0)),
        strict_data=bool(payload.get("strict_data", True)),
    )

    engine = BacktestEngine(config=config)
    result = engine.run_from_files(
        data_path=data_path,
        strategy_path=strategy_path,
        strategy_params=payload.get("strategy_params") or {},
    )

    return {
        "status": "SUCCESS",
        "metrics": normalize_metrics(result.to_dict().get("summary", {})),
        "errorMessage": None,
        "artifacts": build_artifacts(result.to_dict()),
    }


def main() -> int:
    raw_input = sys.stdin.read()
    if not raw_input.strip():
        output = failure_output("Empty input payload")
        sys.stdout.write(json.dumps(output))
        sys.stdout.write("\n")
        return 2

    try:
        payload = json.loads(raw_input)
        if not isinstance(payload, dict):
            raise ValueError("Input payload must be a JSON object")
    except (json.JSONDecodeError, ValueError) as exc:
        output = failure_output(f"Invalid JSON payload: {exc}")
        sys.stdout.write(json.dumps(output))
        sys.stdout.write("\n")
        return 2

    try:
        output = execute_run(payload)
        exit_code = 0
    except Exception as exc:
        output = failure_output(str(exc))
        exit_code = 1

    sys.stdout.write(json.dumps(output))
    sys.stdout.write("\n")
    return exit_code


def normalize_metrics(summary: dict[str, Any]) -> dict[str, Any]:
    return {
        "netProfit": round_number(summary.get("net_profit")),
        "winRate": to_percent(summary.get("win_rate")),
        "maxDrawdown": to_percent(summary.get("max_drawdown")),
        "sharpe": round_number(summary.get("sharpe"), digits=4),
        "trades": to_int(summary.get("number_of_trades")),
    }


def build_artifacts(result: dict[str, Any]) -> dict[str, str] | None:
    trades_path = write_artifact_file("backtest-trades-", result.get("trades"))
    equity_curve_path = write_artifact_file("backtest-equity-", result.get("equity_curve"))
    if trades_path is None and equity_curve_path is None:
        return None

    return {
        "equityCurvePath": equity_curve_path,
        "tradesPath": trades_path,
    }


def write_artifact_file(prefix: str, payload: Any) -> str | None:
    if payload is None:
        return None

    with tempfile.NamedTemporaryFile(
        mode="w",
        suffix=".json",
        prefix=prefix,
        delete=False,
        encoding="utf-8",
    ) as artifact_file:
        json.dump(make_json_safe(payload), artifact_file)
        artifact_file.write("\n")
        return str(Path(artifact_file.name))


def make_json_safe(value: Any) -> Any:
    if isinstance(value, dict):
        return {key: make_json_safe(item) for key, item in value.items()}
    if isinstance(value, list):
        return [make_json_safe(item) for item in value]
    if isinstance(value, float):
        return round(value, 4)
    return value


def failure_output(message: str) -> dict[str, Any]:
    return {
        "status": "FAILED",
        "metrics": None,
        "errorMessage": message or "Backtest execution failed",
        "artifacts": None,
    }


def round_number(value: Any, digits: int = 2) -> float:
    try:
        return round(float(value), digits)
    except (TypeError, ValueError):
        return 0.0


def to_percent(value: Any) -> float:
    try:
        numeric = float(value)
    except (TypeError, ValueError):
        return 0.0

    return round(numeric * 100, 2)


def to_int(value: Any) -> int:
    try:
        return int(value)
    except (TypeError, ValueError):
        return 0


if __name__ == "__main__":
    raise SystemExit(main())
