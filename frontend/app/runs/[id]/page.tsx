"use client";

import { useMemo, useState } from "react";
import { useParams } from "next/navigation";
import { Expand, Minimize2 } from "lucide-react";
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
  TradesAnalyzerChart,
} from "@/features/runs/charts/run-charts";
import { MetricCard } from "@/features/runs/components/metric-card";
import { RunHeader } from "@/features/runs/components/run-header";
import { TradesTable } from "@/features/runs/components/trades-table";
import { useRuns } from "@/features/runs/store/run-store";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Badge } from "@/components/ui/badge";
import { previewRows } from "@/lib/demo-data/datasets";
import { logLines } from "@/lib/demo-data/logs";
import { trades } from "@/lib/demo-data/trades";

export default function RunDetailsPage() {
  const params = useParams();
  const { getRunById, isLoading } = useRuns();
  const [isAnalyzerFullscreen, setIsAnalyzerFullscreen] = useState(false);
  const runId = Array.isArray(params?.id) ? params?.id[0] : params?.id;
  const run = runId ? getRunById(runId) : undefined;
  const tradeSummary = useMemo(() => {
    const wins = trades.filter((trade) => trade.pnl > 0).length;
    const losses = trades.filter((trade) => trade.pnl <= 0).length;
    const avgDuration =
      trades.length > 0
        ? trades.reduce((sum, trade) => sum + Number(trade.duration.replace("d", "")), 0) /
          trades.length
        : 0;
    const totalPnl = trades.reduce((sum, trade) => sum + trade.pnl, 0);

    return {
      wins,
      losses,
      totalPnl,
      avgDuration,
    };
  }, []);

  if (isLoading) {
    return <LoadingState label="Загрузка запуска..." />;
  }

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
    <div className="flex min-h-full flex-col gap-4 2xl:gap-3">
      <PageHeader
        eyebrow="Детали запуска"
        title={`Запуск ${run.id}`}
      />
      <RunHeader run={run} />

      <div className="grid grid-cols-2 gap-3 lg:grid-cols-6">
        <MetricCard label="PnL" value={`${run.metrics.pnl.toFixed(1)}%`} tone="profit" />
        <MetricCard label="Шарп" value={run.metrics.sharpe.toFixed(2)} />
        <MetricCard label="Макс. просадка" value={`${run.metrics.maxDrawdown.toFixed(1)}%`} tone="loss" />
        <MetricCard label="Винрейт" value={`${run.metrics.winrate.toFixed(1)}%`} />
        <MetricCard label="Сделки" value={`${run.metrics.trades}`} />
        <MetricCard label="Влияние комиссий" value={`${run.metrics.feesImpact.toFixed(1)}%`} />
      </div>

      <div className="grid grid-cols-1 gap-3 md:grid-cols-2 2xl:grid-cols-4">
        <ChartCard
          title="Кривая капитала"
          subtitle="Рост портфеля во времени"
          chartClassName="h-56 xl:h-60 2xl:h-64"
        >
          <EquityChart />
        </ChartCard>
        <ChartCard
          title="Просадка"
          subtitle="Снижение от пика до минимума"
          chartClassName="h-56 xl:h-60 2xl:h-64"
        >
          <DrawdownChart />
        </ChartCard>
        <ChartCard
          title="Отставание от пика"
          subtitle="Отклонение от максимума капитала"
          chartClassName="h-56 xl:h-60 2xl:h-64"
        >
          <UnderwaterChart />
        </ChartCard>
        <ChartCard
          title="Гистограмма доходности"
          subtitle="Распределение доходностей"
          chartClassName="h-56 xl:h-60 2xl:h-64"
        >
          <ReturnsHistogramChart />
        </ChartCard>
      </div>

      <div className="grid grid-cols-1 gap-3 2xl:grid-cols-[minmax(0,1.45fr)_minmax(0,1fr)]">
        <ChartCard
          title="Анализатор сделок"
          actions={
            <Button
              type="button"
              size="icon"
              variant="secondary"
              onClick={() => setIsAnalyzerFullscreen(true)}
              aria-label="Развернуть график цены на весь экран"
              title="Развернуть график цены на весь экран"
              className="h-8 w-8 border border-border/80 bg-panel-subtle text-foreground transition hover:border-white hover:bg-white hover:text-black hover:shadow-[0_0_14px_rgba(255,255,255,0.6)]"
            >
              <Expand className="h-4 w-4" />
            </Button>
          }
          chartClassName="h-64 xl:h-72 2xl:h-[420px]"
        >
          <TradesAnalyzerChart datasetRows={previewRows} trades={trades} />
        </ChartCard>

        <SurfaceCard
          title="Журнал сделок"
          contentClassName="p-0"
        >
          <div className="grid grid-cols-2 gap-3 p-4 text-xs">
            <div className="rounded-[14px] border border-border bg-panel-subtle p-3">
              <div className="text-[11px] uppercase text-muted-foreground">Профитных</div>
              <div className="text-sm font-medium text-status-success">{tradeSummary.wins}</div>
            </div>
            <div className="rounded-[14px] border border-border bg-panel-subtle p-3">
              <div className="text-[11px] uppercase text-muted-foreground">Убыточных</div>
              <div className="text-sm font-medium text-status-failed">{tradeSummary.losses}</div>
            </div>
            <div className="rounded-[14px] border border-border bg-panel-subtle p-3">
              <div className="text-[11px] uppercase text-muted-foreground">Суммарный PnL</div>
              <div className={tradeSummary.totalPnl >= 0 ? "text-sm font-medium text-profit" : "text-sm font-medium text-loss"}>
                {tradeSummary.totalPnl.toFixed(2)}%
              </div>
            </div>
            <div className="rounded-[14px] border border-border bg-panel-subtle p-3">
              <div className="text-[11px] uppercase text-muted-foreground">Средняя длительность</div>
              <div className="text-sm font-medium text-foreground">{tradeSummary.avgDuration.toFixed(1)}d</div>
            </div>
          </div>
          <div className="max-h-[420px] overflow-y-auto">
            <TradesTable rows={trades} />
          </div>
        </SurfaceCard>
      </div>

      {isAnalyzerFullscreen ? (
        <div className="fixed inset-0 z-[100] bg-background/88 p-4 backdrop-blur-sm md:p-6">
          <div className="mx-auto flex h-full w-full max-w-[2000px] flex-col overflow-hidden rounded-[26px] border border-border bg-panel shadow-[0_30px_80px_rgba(0,0,0,0.45)]">
            <div className="flex items-center justify-between border-b border-border px-5 py-4">
              <div className="text-sm font-semibold text-foreground">
                Анализатор сделок - полный экран
              </div>
              <Button
                type="button"
                size="sm"
                variant="secondary"
                onClick={() => setIsAnalyzerFullscreen(false)}
                className="h-8 border border-border/80 bg-panel-subtle text-xs text-foreground transition hover:border-white hover:bg-white hover:text-black hover:shadow-[0_0_14px_rgba(255,255,255,0.6)]"
              >
                <Minimize2 className="mr-2 h-4 w-4" />
                Свернуть
              </Button>
            </div>
            <div className="min-h-0 flex-1 p-3 md:p-5">
              <div className="h-full rounded-[20px] border border-border bg-panel-subtle p-2 md:p-4">
                <TradesAnalyzerChart datasetRows={previewRows} trades={trades} />
              </div>
            </div>
          </div>
        </div>
      ) : null}

      <div className="grid grid-cols-1 gap-3 xl:grid-cols-2">
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
              <ScrollArea className="h-[180px] font-mono text-xs text-muted-foreground">
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
