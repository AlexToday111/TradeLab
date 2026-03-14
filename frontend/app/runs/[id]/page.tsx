"use client";

import { useParams } from "next/navigation";
import { ChartCard } from "@/components/shared/chart-card";
import { EmptyState } from "@/components/shared/empty-state";
import { LoadingState } from "@/components/shared/loading-state";
import { PageHeader } from "@/components/shared/page-header";
import { SurfaceCard } from "@/components/shared/surface-card";
import {
  EquityChart,
  DrawdownChart,
  UnderwaterChart,
  ReturnsHistogramChart,
} from "@/features/runs/charts/run-charts";
import { MetricCard } from "@/features/runs/components/metric-card";
import { RunHeader } from "@/features/runs/components/run-header";
import { TradesTable } from "@/features/runs/components/trades-table";
import { useRuns } from "@/features/runs/store/run-store";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Badge } from "@/components/ui/badge";
import { logLines } from "@/lib/demo-data/logs";
import { trades } from "@/lib/demo-data/trades";

export default function RunDetailsPage() {
  const params = useParams();
  const { getRunById } = useRuns();
  const runId = Array.isArray(params?.id) ? params?.id[0] : params?.id;
  const run = runId ? getRunById(runId) : undefined;

  if (!run) {
    return (
      <EmptyState
        title="Запуск не найден"
        description="Выберите запуск в разделе бэктестов, чтобы посмотреть детали."
        actionLabel="Перейти к бэктестам"
        actionHref="/backtests"
      />
    );
  }

  const configJson = JSON.stringify(
    {
      fees: run.params.fees,
      slippage: run.params.slippage,
      execution: run.params.execution,
      risk: {
        per_trade: run.params.riskPerTrade,
        max_exposure: run.params.maxExposure,
      },
    },
    null,
    2
  );

  return (
    <div className="flex h-full flex-col gap-5">
      <PageHeader
        eyebrow="Детали запуска"
        title={`Запуск ${run.id}`}
        description="Метрики, графики, артефакты и воспроизводимость выбранного прогона."
      />
      <RunHeader run={run} />

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-6">
        <MetricCard label="PnL" value={`${run.metrics.pnl.toFixed(1)}%`} tone="profit" />
        <MetricCard label="Шарп" value={run.metrics.sharpe.toFixed(2)} />
        <MetricCard label="Макс. просадка" value={`${run.metrics.maxDrawdown.toFixed(1)}%`} tone="loss" />
        <MetricCard label="Винрейт" value={`${run.metrics.winrate.toFixed(1)}%`} />
        <MetricCard label="Сделки" value={`${run.metrics.trades}`} />
        <MetricCard label="Влияние комиссий" value={`${run.metrics.feesImpact.toFixed(1)}%`} />
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <ChartCard title="Кривая капитала" subtitle="Рост портфеля во времени">
          <EquityChart />
        </ChartCard>
        <ChartCard title="Просадка" subtitle="Снижение от пика до минимума">
          <DrawdownChart />
        </ChartCard>
        <ChartCard title="Отставание от пика" subtitle="Отклонение от максимума капитала">
          <UnderwaterChart />
        </ChartCard>
        <ChartCard title="Гистограмма доходности" subtitle="Распределение доходностей">
          <ReturnsHistogramChart />
        </ChartCard>
      </div>

      <SurfaceCard
        title="Сделки"
        subtitle="Отфильтровано по выбранному запуску."
        contentClassName="p-0"
      >
        <TradesTable rows={trades} />
      </SurfaceCard>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <SurfaceCard contentClassName="p-0">
          <Tabs defaultValue="logs" className="flex h-full flex-col">
            <TabsList className="h-9 rounded-none border-b border-border bg-panel px-3">
              <TabsTrigger value="logs" className="text-xs">
                Логи
              </TabsTrigger>
              <TabsTrigger value="artifacts" className="text-xs">
                Артефакты
              </TabsTrigger>
            </TabsList>
            <TabsContent value="logs" className="flex-1 p-3">
              <ScrollArea className="h-[220px] font-mono text-xs text-muted-foreground">
                {logLines.map((line) => (
                  <div key={line}>{line}</div>
                ))}
              </ScrollArea>
            </TabsContent>
            <TabsContent value="artifacts" className="flex-1 p-3">
              <div className="space-y-2 text-xs text-muted-foreground">
                {run.artifacts.length === 0 ? (
                  run.status === "running" || run.status === "queued" ? (
                    <LoadingState label="Артефакты формируются..." />
                  ) : (
                    <div className="rounded-md border border-border bg-panel-subtle p-2">
                      Артефактов пока нет.
                    </div>
                  )
                ) : (
                  run.artifacts.map((artifact) => (
                    <div
                      key={artifact.id}
                      className="flex items-center justify-between rounded-md border border-border bg-panel-subtle p-2"
                    >
                      <div>{artifact.label}</div>
                      <Badge variant="secondary">{artifact.size}</Badge>
                    </div>
                  ))
                )}
              </div>
            </TabsContent>
          </Tabs>
        </SurfaceCard>

        <SurfaceCard
          title="Воспроизводимость"
          subtitle="Коммит, версия датасета и исходный конфиг."
        >
          <div className="p-4 text-xs text-muted-foreground">
            <div className="mb-3 grid grid-cols-2 gap-3">
              <div className="rounded-md border border-border bg-panel-subtle p-2">
                <div className="text-[11px] uppercase">Коммит</div>
                <div className="font-mono text-foreground">{run.commit}</div>
              </div>
              <div className="rounded-md border border-border bg-panel-subtle p-2">
                <div className="text-[11px] uppercase">Датасет</div>
                <div className="text-foreground">{run.datasetVersion}</div>
              </div>
            </div>
            <Tabs defaultValue="yaml">
              <TabsList className="h-8 bg-panel-subtle">
                <TabsTrigger value="yaml" className="text-xs">
                  YAML
                </TabsTrigger>
                <TabsTrigger value="json" className="text-xs">
                  JSON
                </TabsTrigger>
              </TabsList>
              <TabsContent value="yaml" className="mt-2">
                <pre className="whitespace-pre-wrap rounded-md border border-border bg-panel-subtle p-3 font-mono text-[11px] text-foreground">
                  {run.config}
                </pre>
              </TabsContent>
              <TabsContent value="json" className="mt-2">
                <pre className="whitespace-pre-wrap rounded-md border border-border bg-panel-subtle p-3 font-mono text-[11px] text-foreground">
                  {configJson}
                </pre>
              </TabsContent>
            </Tabs>
          </div>
        </SurfaceCard>
      </div>
    </div>
  );
}
