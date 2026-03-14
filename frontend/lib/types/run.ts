export type RunStatus = "queued" | "running" | "done" | "failed";

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
};

export type Run = {
  id: string;
  strategy: string;
  datasetVersion: string;
  period: string;
  timeframe: string;
  params: RunParams;
  metrics: RunMetrics;
  status: RunStatus;
  artifacts: RunArtifact[];
  createdAt: string;
  commit: string;
  config: string;
  tags: string[];
  diff: {
    code: boolean;
    data: boolean;
    config: boolean;
  };
};
