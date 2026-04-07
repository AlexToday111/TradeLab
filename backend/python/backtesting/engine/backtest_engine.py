from __future__ import annotations

from dataclasses import dataclass, field

import pandas as pd

from backtesting.data.data_loader import DataLoader
from backtesting.engine.event_loop import EventLoop
from backtesting.engine.execution import ExecutionEngine
from backtesting.engine.portfolio import Portfolio
from backtesting.engine.position_sizing import PositionSizingConfig
from backtesting.metrics.performance import PerformanceMetricsCalculator
from backtesting.models.result import BacktestResult
from backtesting.strategy.base import Strategy
from backtesting.strategy.context import StrategyContext
from backtesting.strategy.loader import StrategyLoader
from backtesting.utils.validation import validate_ohlcv_dataframe


@dataclass(frozen=True, slots=True)
class BacktestConfig:
    initial_cash: float = 10_000.0
    fee_rate: float = 0.0
    slippage_bps: float = 0.0
    strict_data: bool = True
    position_sizing: PositionSizingConfig = field(default_factory=PositionSizingConfig)


class BacktestEngine:
    def __init__(self, *, config: BacktestConfig | None = None) -> None:
        self._config = config or BacktestConfig()
        self._metrics = PerformanceMetricsCalculator()

    def run(self, *, data: pd.DataFrame, strategy: Strategy) -> BacktestResult:
        validated_data = validate_ohlcv_dataframe(data, strict=self._config.strict_data)
        portfolio = Portfolio(initial_cash=self._config.initial_cash)
        context = StrategyContext(
            params=strategy.params,
            portfolio=portfolio,
            position_sizing=self._config.position_sizing,
        )
        event_loop = EventLoop(
            ExecutionEngine(
                fee_rate=self._config.fee_rate,
                slippage_bps=self._config.slippage_bps,
                position_sizing=self._config.position_sizing,
            )
        )
        loop_result = event_loop.run(
            data=validated_data,
            strategy=strategy,
            context=context,
            portfolio=portfolio,
        )
        summary = self._metrics.calculate(
            starting_equity=self._config.initial_cash,
            equity_curve=loop_result.equity_curve,
            trades=portfolio.trades,
        )
        return BacktestResult(
            summary=summary,
            trades=portfolio.trades,
            equity_curve=loop_result.equity_curve,
            logs=loop_result.logs,
            warnings=loop_result.warnings,
        )

    def run_from_files(
        self,
        *,
        data_path: str,
        strategy_path: str,
        strategy_params: dict | None = None,
    ) -> BacktestResult:
        data = DataLoader(strict=self._config.strict_data).load_csv(data_path)
        strategy = StrategyLoader().load(strategy_path, params=strategy_params or {})
        return self.run(data=data, strategy=strategy)
