from __future__ import annotations

from math import inf

from backtesting.models.result import EquityPoint
from backtesting.models.trade import Trade


class PerformanceMetricsCalculator:
    def calculate(
        self,
        *,
        starting_equity: float,
        equity_curve: list[EquityPoint],
        trades: list[Trade],
    ) -> dict[str, float | int]:
        ending_equity = equity_curve[-1].equity if equity_curve else starting_equity
        net_profit = ending_equity - starting_equity
        total_return = (net_profit / starting_equity) if starting_equity else 0.0

        trade_pnls = [trade.pnl for trade in trades]
        wins = [pnl for pnl in trade_pnls if pnl > 0]
        losses = [pnl for pnl in trade_pnls if pnl < 0]
        gross_profit = sum(wins)
        gross_loss = abs(sum(losses))
        profit_factor = (
            inf
            if gross_loss == 0 and gross_profit > 0
            else (gross_profit / gross_loss if gross_loss else 0.0)
        )

        return {
            "total_return": total_return,
            "net_profit": net_profit,
            "max_drawdown": self._calculate_max_drawdown(equity_curve, starting_equity),
            "win_rate": (len(wins) / len(trades)) if trades else 0.0,
            "profit_factor": profit_factor,
            "avg_win": (gross_profit / len(wins)) if wins else 0.0,
            "avg_loss": (sum(losses) / len(losses)) if losses else 0.0,
            "number_of_trades": len(trades),
        }

    @staticmethod
    def _calculate_max_drawdown(equity_curve: list[EquityPoint], starting_equity: float) -> float:
        peak = starting_equity
        max_drawdown = 0.0

        for point in equity_curve:
            peak = max(peak, point.equity)
            if peak == 0:
                continue
            drawdown = (peak - point.equity) / peak
            max_drawdown = max(max_drawdown, drawdown)

        return max_drawdown
