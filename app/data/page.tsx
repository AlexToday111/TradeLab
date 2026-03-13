"use client";

import { Database, PlugZap } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { ChartCard } from "@/components/shared/chart-card";
import { PageHeader } from "@/components/shared/page-header";
import { SurfaceCard } from "@/components/shared/surface-card";
import { OhlcPreviewChart } from "@/features/runs/charts/run-charts";
import {
  dataSources,
  datasetVersions,
  pipelineSteps,
  dataQuality,
  previewRows,
} from "@/lib/demo-data/datasets";
import { getDataSourceStatusLabel, getDataSourceTypeLabel } from "@/lib/ui-text";

export default function DataPage() {
  return (
    <div className="flex h-full flex-col gap-5">
      <PageHeader
        eyebrow="Данные"
        title="Данные"
        description="Источники, пайплайны, версии датасетов и качество подготовки."
        actions={
          <Button size="sm">
            <PlugZap className="mr-2 h-4 w-4" />
            Использовать в бэктесте
          </Button>
        }
      />

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        {dataSources.map((source) => (
          <SurfaceCard
            key={source.id}
            className="py-0"
            contentClassName="flex items-center justify-between p-4"
          >
            <div className="flex items-center gap-3">
              <div className="rounded-[14px] border border-border bg-panel-subtle p-2">
                <Database className="h-4 w-4 text-muted-foreground" />
              </div>
              <div>
                <div className="text-sm font-medium text-foreground">
                  {source.name}
                </div>
                <div className="text-xs text-muted-foreground">
                  {getDataSourceTypeLabel(source.type)}
                </div>
              </div>
            </div>
            <Badge
              className={
                source.status === "connected"
                  ? "border border-status-success/40 bg-status-success/20 text-status-success"
                  : "border border-border bg-secondary text-muted-foreground"
              }
            >
              {getDataSourceStatusLabel(source.status)}
            </Badge>
          </SurfaceCard>
        ))}
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <SurfaceCard title="Конструктор пайплайна" className="lg:col-span-2">
          <div className="flex flex-wrap gap-2">
            {pipelineSteps.map((step, index) => (
              <div
                key={step}
                className="flex items-center gap-2 rounded-full border border-border bg-panel-subtle px-3 py-1 text-xs text-muted-foreground"
              >
                <span className="text-foreground">{index + 1}</span>
                {step}
              </div>
            ))}
          </div>
        </SurfaceCard>
        <SurfaceCard title="Качество данных">
          <div className="grid grid-cols-2 gap-3 text-xs text-muted-foreground">
            {dataQuality.map((item) => (
              <div
                key={item.label}
                className="rounded-[18px] border border-border bg-panel-subtle p-3"
              >
                <div className="text-[11px] uppercase">{item.label}</div>
                <div className="text-foreground">{item.value}</div>
              </div>
            ))}
          </div>
        </SurfaceCard>
      </div>

      <SurfaceCard
        title="Версии датасетов"
        subtitle="Версионированные датасеты с хешами пайплайнов."
        contentClassName="p-0"
      >
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Версия</TableHead>
              <TableHead>Период</TableHead>
              <TableHead>Таймфрейм</TableHead>
              <TableHead>Символы</TableHead>
              <TableHead>Размер</TableHead>
              <TableHead>Хеш пайплайна</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {datasetVersions.map((dataset) => (
              <TableRow key={dataset.id}>
                <TableCell className="font-medium text-foreground">
                  {dataset.name}
                </TableCell>
                <TableCell className="text-xs text-muted-foreground">
                  {dataset.period}
                </TableCell>
                <TableCell className="text-xs text-muted-foreground">
                  {dataset.timeframe}
                </TableCell>
                <TableCell className="text-xs text-muted-foreground">
                  {dataset.symbols.join(", ")}
                </TableCell>
                <TableCell className="text-xs text-muted-foreground">
                  {dataset.size}
                </TableCell>
                <TableCell className="text-xs text-muted-foreground">
                  {dataset.pipelineHash}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </SurfaceCard>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <ChartCard title="Предпросмотр таблицы" subtitle="Упрощенный OHLC и объем">
          <OhlcPreviewChart />
        </ChartCard>
        <SurfaceCard
          title="Таблица предпросмотра"
          subtitle='Первые строки из датасета "Акции США v13".'
          className="lg:col-span-2"
          contentClassName="p-0"
        >
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Временная метка</TableHead>
                <TableHead>Открытие</TableHead>
                <TableHead>Макс.</TableHead>
                <TableHead>Мин.</TableHead>
                <TableHead>Закрытие</TableHead>
                <TableHead>Объем</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {previewRows.map((row) => (
                <TableRow key={row.ts}>
                  <TableCell className="text-xs text-muted-foreground">
                    {row.ts}
                  </TableCell>
                  <TableCell className="text-xs text-muted-foreground">
                    {row.open}
                  </TableCell>
                  <TableCell className="text-xs text-muted-foreground">
                    {row.high}
                  </TableCell>
                  <TableCell className="text-xs text-muted-foreground">
                    {row.low}
                  </TableCell>
                  <TableCell className="text-xs text-muted-foreground">
                    {row.close}
                  </TableCell>
                  <TableCell className="text-xs text-muted-foreground">
                    {row.volume}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </SurfaceCard>
      </div>
    </div>
  );
}
