from __future__ import annotations

import json
import sys

from backtesting.engine.backtest_engine import BacktestConfig, BacktestEngine


def main() -> int:
    raw_input = sys.stdin.read()
    if not raw_input.strip():
        sys.stderr.write("Empty input payload\n")
        return 2

    try:
        payload = json.loads(raw_input)
    except json.JSONDecodeError as exc:
        sys.stderr.write(f"Invalid JSON payload: {exc}\n")
        return 2

    data_path = payload.get("data_path")
    strategy_path = payload.get("strategy_path")
    if not data_path or not strategy_path:
        sys.stderr.write("Missing required fields: data_path and strategy_path\n")
        return 2

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

    sys.stdout.write(json.dumps(result.to_dict()))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
