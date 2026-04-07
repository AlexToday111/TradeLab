from __future__ import annotations

from dataclasses import dataclass
from decimal import Decimal

import pandas as pd

from backtesting.data.models import Candle
from backtesting.engine.execution import ExecutionEngine
from backtesting.engine.portfolio import Portfolio
from backtesting.models.result import EquityPoint
from backtesting.strategy.base import Strategy
from backtesting.strategy.context import StrategyContext


@dataclass(frozen=True, slots=True)
class EventLoopResult:
    equity_curve: list[EquityPoint]
    logs: list[str]
    warnings: list[str]


class EventLoop:
    def __init__(self, execution_engine: ExecutionEngine) -> None:
        self._execution_engine = execution_engine

    def run(
        self,
        *,
        data: pd.DataFrame,
        strategy: Strategy,
        context: StrategyContext,
        portfolio: Portfolio,
    ) -> EventLoopResult:
        equity_curve: list[EquityPoint] = []
        logs: list[str] = []
        warnings: list[str] = []
        strategy.initialize(context)
        pending_intents = context.drain_order_intents()

        for index, row in data.iterrows():
            bar = Candle(
                timestamp=row["timestamp"].to_pydatetime(),
                open=float(row["open"]),
                high=float(row["high"]),
                low=float(row["low"]),
                close=float(row["close"]),
                volume=float(row["volume"]),
            )

            fills = self._execution_engine.execute(
                pending_intents,
                bar=bar,
                portfolio=portfolio,
            )
            for fill in fills:
                logs.append(
                    "Executed "
                    f"{fill.side} {fill.quantity:.8f} @ {fill.price:.8f} "
                    f"on {fill.executed_at.isoformat()}"
                )

            portfolio.mark_to_market(Decimal(str(bar.close)))
            context.update_market_state(
                historical_data=data.iloc[: index + 1],
                current_bar_time=bar.timestamp,
            )
            strategy.on_bar(bar, context)
            pending_intents = context.drain_order_intents()

            equity_curve.append(
                EquityPoint(
                    timestamp=bar.timestamp,
                    cash=float(portfolio.cash),
                    equity=float(portfolio.equity),
                    position_size=float(portfolio.position_size),
                )
            )

        strategy.finalize(context)
        final_intents = context.drain_order_intents()
        discarded_intents = len(pending_intents) + len(final_intents)
        if discarded_intents:
            warnings.append(
                f"{discarded_intents} order intents were not executed because "
                "no next candle was available"
            )

        return EventLoopResult(equity_curve=equity_curve, logs=logs, warnings=warnings)
