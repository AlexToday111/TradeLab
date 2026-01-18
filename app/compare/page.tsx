"use client";

import { useMemo, useState } from "react";
import { useRuns } from "@/components/run/run-store";
import { Checkbox } from "@/components/ui/checkbox";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { ChartCard } from "@/components/charts/chart-card";
import { EmptyState } from "@/components/layout/empty-state";
import { ResponsiveContainer, LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid } from "recharts";
import { equityCurve } from "@/lib/mock-data/charts";

const lineColors = [
  "hsl(var(--chart-1))",
  "hsl(var(--chart-2))",
  "hsl(var(--chart-3))",
  "hsl(var(--chart-4))",
  "hsl(var(--chart-5))",
];

export default function CompareRunsPage() {
  const { runs } = useRuns();
  const [selectedIds, setSelectedIds] = useState<string[]>([]);

  const toggleRun = (id: string) => {
    setSelectedIds((prev) => {
      if (prev.includes(id)) {
        return prev.filter((item) => item !== id);
      }
      if (prev.length >= 5) {
        return prev;
      }
      return [...prev, id];
    });
  };

  const selectedRuns = useMemo(
    () => runs.filter((run) => selectedIds.includes(run.id)),
    [runs, selectedIds]
  );

  const overlayData = useMemo(() => {
    return equityCurve.map((point, index) => {
      const row: Record<string, number | string> = { date: point.date };
      selectedRuns.forEach((run, idx) => {
        row[run.id] = point.value + Math.sin(index / 2 + idx) * 2 + idx * 3;
      });
      return row;
    });
  }, [selectedRuns]);

  const configLeft = selectedRuns[0];
  const configRight = selectedRuns[1];

  return (
    <div className="flex h-full flex-col gap-4">
      <div>
        <div className="text-lg font-semibold text-foreground">Compare runs</div>
        <div className="text-xs text-muted-foreground">
          Select 2-5 runs to overlay results and inspect diffs.
        </div>
      </div>

      <div className="rounded-lg border border-border bg-panel p-3">
        <div className="mb-2 text-xs text-muted-foreground">Runs</div>
        <div className="grid grid-cols-1 gap-2 md:grid-cols-2 xl:grid-cols-3">
          {runs.map((run) => (
            <label
              key={run.id}
              className="flex cursor-pointer items-center justify-between rounded-md border border-border bg-panel-subtle p-2 text-xs"
            >
              <div>
                <div className="font-mono text-foreground">{run.id}</div>
                <div className="text-muted-foreground">
                  {run.strategy} / {run.datasetVersion}
                </div>
              </div>
              <Checkbox
                checked={selectedIds.includes(run.id)}
                onCheckedChange={() => toggleRun(run.id)}
              />
            </label>
          ))}
        </div>
      </div>

      {selectedRuns.length < 2 ? (
        <EmptyState
          title="Select at least two runs"
          description="Overlay charts, compare metrics, and inspect config diffs."
        />
      ) : (
        <>
          <ChartCard title="Overlay equity curves" subtitle="Selected runs">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={overlayData}>
                <CartesianGrid stroke="hsl(var(--border))" strokeDasharray="4 4" />
                <XAxis
                  dataKey="date"
                  tick={{ fill: "hsl(var(--muted-foreground))", fontSize: 11 }}
                  axisLine={{ stroke: "hsl(var(--border))" }}
                  tickLine={{ stroke: "hsl(var(--border))" }}
                />
                <YAxis
                  tick={{ fill: "hsl(var(--muted-foreground))", fontSize: 11 }}
                  axisLine={{ stroke: "hsl(var(--border))" }}
                  tickLine={{ stroke: "hsl(var(--border))" }}
                />
                <Tooltip
                  contentStyle={{
                    background: "hsl(var(--popover))",
                    border: "1px solid hsl(var(--border))",
                    color: "hsl(var(--foreground))",
                    fontSize: "12px",
                  }}
                />
                {selectedRuns.map((run, idx) => (
                  <Line
                    key={run.id}
                    type="monotone"
                    dataKey={run.id}
                    stroke={lineColors[idx % lineColors.length]}
                    strokeWidth={2}
                    dot={false}
                  />
                ))}
              </LineChart>
            </ResponsiveContainer>
          </ChartCard>

          <div className="rounded-lg border border-border bg-panel">
            <div className="border-b border-border px-4 py-3">
              <div className="text-sm font-semibold text-foreground">Metrics</div>
              <div className="text-xs text-muted-foreground">Delta by run.</div>
            </div>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Run</TableHead>
                  <TableHead>PnL</TableHead>
                  <TableHead>Sharpe</TableHead>
                  <TableHead>Max DD</TableHead>
                  <TableHead>Trades</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {selectedRuns.map((run) => (
                  <TableRow key={run.id}>
                    <TableCell className="font-mono text-xs text-foreground">
                      {run.id}
                    </TableCell>
                    <TableCell className="text-xs text-profit">
                      {run.metrics.pnl.toFixed(1)}%
                    </TableCell>
                    <TableCell className="text-xs text-muted-foreground">
                      {run.metrics.sharpe.toFixed(2)}
                    </TableCell>
                    <TableCell className="text-xs text-loss">
                      {run.metrics.maxDrawdown.toFixed(1)}%
                    </TableCell>
                    <TableCell className="text-xs text-muted-foreground">
                      {run.metrics.trades}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>

          <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
            <div className="rounded-lg border border-border bg-panel">
              <div className="border-b border-border px-4 py-3">
                <div className="text-sm font-semibold text-foreground">Config diff</div>
                <div className="text-xs text-muted-foreground">Side-by-side parameters.</div>
              </div>
              <div className="grid grid-cols-2 gap-3 p-4 text-xs">
                <div className="rounded-md border border-border bg-panel-subtle p-3">
                  <div className="mb-2 text-[11px] uppercase text-muted-foreground">
                    {configLeft?.id}
                  </div>
                  <pre className="whitespace-pre-wrap font-mono text-[11px] text-foreground">
{configLeft?.config}
                  </pre>
                </div>
                <div className="rounded-md border border-border bg-panel-subtle p-3">
                  <div className="mb-2 text-[11px] uppercase text-muted-foreground">
                    {configRight?.id}
                  </div>
                  <pre className="whitespace-pre-wrap font-mono text-[11px] text-foreground">
{configRight?.config}
                  </pre>
                </div>
              </div>
            </div>

            <div className="rounded-lg border border-border bg-panel">
              <div className="border-b border-border px-4 py-3">
                <div className="text-sm font-semibold text-foreground">Dataset diff</div>
                <div className="text-xs text-muted-foreground">Versions + quality.</div>
              </div>
              <div className="space-y-3 p-4 text-xs text-muted-foreground">
                <div className="rounded-md border border-border bg-panel-subtle p-3">
                  <div className="text-[11px] uppercase">Left</div>
                  <div className="text-foreground">{configLeft?.datasetVersion}</div>
                  <div>Coverage 98.7% / Gaps 0.6%</div>
                </div>
                <div className="rounded-md border border-border bg-panel-subtle p-3">
                  <div className="text-[11px] uppercase">Right</div>
                  <div className="text-foreground">{configRight?.datasetVersion}</div>
                  <div>Coverage 97.9% / Gaps 0.9%</div>
                </div>
                <div className="flex flex-wrap gap-2">
                  <Badge variant="secondary">pipeline hash diff</Badge>
                  <Badge variant="secondary">outlier spikes</Badge>
                  <Badge variant="secondary">coverage delta</Badge>
                </div>
              </div>
            </div>
          </div>

          <div className="rounded-lg border border-border bg-panel p-4 text-xs text-muted-foreground">
            <div className="mb-2 text-sm font-semibold text-foreground">
              Stability hints
            </div>
            <ul className="list-disc space-y-1 pl-4">
              <li>Dataset v13 adds post-2022 volatility spikes.</li>
              <li>Config differs in slippage and execution model.</li>
              <li>Trades count increased with shorter lookback.</li>
            </ul>
          </div>
        </>
      )}
    </div>
  );
}
