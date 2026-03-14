import { Run } from "@/lib/types";

const sampleConfigs = [
  `fees: 0.8\nslippage: 0.5\nexecution: vwap\nrisk:\n  per_trade: 0.5%\n  max_exposure: 25%\n`,
  `fees: 1.2\nslippage: 0.7\nexecution: market\nrisk:\n  per_trade: 0.3%\n  max_exposure: 15%\n`,
];

const sampleStrategies = [
  "atlas_momentum.py",
  "orbit_reversion.py",
  "ridge_carry.py",
];

const sampleDatasets = [
  "Акции США v13",
  "ETF внутри дня v21",
  "FX дневной v09",
];

export function createMockRun(): Run {
  const id = `run_${Math.random().toString(16).slice(2, 6)}`;
  const strategy = sampleStrategies[Math.floor(Math.random() * sampleStrategies.length)];
  const datasetVersion = sampleDatasets[Math.floor(Math.random() * sampleDatasets.length)];
  const config = sampleConfigs[Math.floor(Math.random() * sampleConfigs.length)];
  const now = new Date();
  const createdAt = now.toISOString().slice(0, 16).replace("T", " ");

  return {
    id,
    strategy,
    datasetVersion,
    period: "2018-01-01 -> 2024-12-31",
    timeframe: "1D",
    params: {
      fees: "0.8 bps",
      slippage: "0.5 bps",
      execution: "VWAP",
      riskPerTrade: "0.5%",
      maxExposure: "25%",
      symbols: ["SPY", "QQQ", "IWM", "EFA", "EEM"],
      timeframe: "1D",
      period: "2018-01-01 -> 2024-12-31",
    },
    metrics: {
      pnl: 12.6,
      sharpe: 1.24,
      maxDrawdown: -10.4,
      trades: 640,
      winrate: 51.8,
      avgTrade: 0.09,
      feesImpact: -2.0,
    },
    status: "queued",
    artifacts: [],
    createdAt,
    commit: "new_run",
    config,
    tags: ["candidate"],
    diff: { code: true, data: true, config: true },
  };
}
