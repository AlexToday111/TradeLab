import { getProjectRuns, getRunProjectId } from "@/lib/project-runs";
import type { Run } from "@/lib/types";

function makeRun(overrides: Partial<Run>): Run {
  return {
    id: "run-local",
    strategy: "Atlas Momentum",
    datasetVersion: "dataset-v1",
    period: "2024-01",
    timeframe: "1h",
    params: {
      fees: "0.1%",
      slippage: "0.05%",
      execution: "market",
      riskPerTrade: "1%",
      maxExposure: "10%",
      symbols: ["BTCUSDT"],
      timeframe: "1h",
      period: "2024-01",
    },
    metrics: {
      pnl: 0,
      sharpe: 0,
      maxDrawdown: 0,
      trades: 0,
      winrate: 0,
      avgTrade: 0,
      feesImpact: 0,
    },
    status: "done",
    artifacts: [],
    createdAt: "2024-01-01T00:00:00Z",
    commit: "abc123",
    config: "default",
    tags: [],
    diff: {
      code: false,
      data: false,
      config: false,
    },
    ...overrides,
  };
}

describe("project run mapping", () => {
  it("prefers explicit project tags over strategy name heuristics", () => {
    const run = makeRun({
      strategy: "Atlas Momentum",
      tags: ["project:proj-orbit"],
    });

    expect(getRunProjectId(run)).toBe("proj-orbit");
  });

  it("falls back to strategy keywords when no tag is present", () => {
    const run = makeRun({ strategy: "Ridge Breakout" });

    expect(getRunProjectId(run)).toBe("proj-ridge");
  });

  it("filters runs by the resolved project id", () => {
    const runs = [
      makeRun({ id: "run-1", strategy: "Atlas Momentum" }),
      makeRun({ id: "run-2", strategy: "Orbit Rotation" }),
      makeRun({ id: "run-3", strategy: "Custom", tags: ["project:proj-atlas"] }),
    ];

    expect(getProjectRuns(runs, "proj-atlas").map((run) => run.id)).toEqual(["run-1", "run-3"]);
  });
});
