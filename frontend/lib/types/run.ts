export type RunStatus = "queued" | "running" | "done" | "failed" | "canceled";

export type RunMetrics = {
  pnl: number;
  sharpe: number;
  maxDrawdown: number;
  trades: number;
  winrate: number;
  avgTrade: number;
  feesImpact: number;
};

export type RunParams = {
  fees: string;
  slippage: string;
  execution: string;
  riskPerTrade: string;
  maxExposure: string;
  symbols: string[];
  timeframe: string;
  period: string;
};

export type RunArtifact = {
  id: string;
  label: string;
  type: "log" | "report" | "model" | "export";
  size: string;
  downloadUrl?: string;
};

export type Run = {
  id: string;
  backendRunId?: number;
  strategyId?: number;
  strategy: string;
  datasetVersion: string;
  period: string;
  timeframe: string;
  params: RunParams;
  strategyParams?: Record<string, unknown>;
  metrics: RunMetrics;
  status: RunStatus;
  artifacts: RunArtifact[];
  createdAt: string;
  finishedAt?: string | null;
  errorMessage?: string | null;
  exchange?: string;
  symbol?: string;
  from?: string;
  to?: string;
  commit: string;
  config: string;
  tags: string[];
  diff: {
    code: boolean;
    data: boolean;
    config: boolean;
  };
};
