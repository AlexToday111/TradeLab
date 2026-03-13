"use client";

import { Database, PlugZap } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import {
  dataSources,
  datasetVersions,
  pipelineSteps,
  dataQuality,
  previewRows,
} from "@/lib/mock-data/datasets";
import { ChartCard } from "@/components/charts/chart-card";
import { OhlcPreviewChart } from "@/components/charts/run-charts";
import { getDataSourceStatusLabel, getDataSourceTypeLabel } from "@/lib/ui-text";

export default function DataPage() {
  return (
    <div className="flex h-full flex-col gap-4">
      <div>
        <div className="text-lg font-semibold text-foreground">Данные</div>
        <div className="text-xs text-muted-foreground">
          Источники, пайплайны, версии датасетов и проверки качества.
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        {dataSources.map((source) => (
          <Card
            key={source.id}
            className="flex items-center justify-between border-border bg-panel p-4"
          >
            <div className="flex items-center gap-3">
              <div className="rounded-md border border-border bg-panel-subtle p-2">
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
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <Card className="border-border bg-panel p-4 lg:col-span-2">
          <div className="mb-3 text-sm font-semibold text-foreground">
            Конструктор пайплайна
          </div>
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
        </Card>
        <Card className="border-border bg-panel p-4">
          <div className="mb-3 text-sm font-semibold text-foreground">
            Качество данных
          </div>
          <div className="grid grid-cols-2 gap-3 text-xs text-muted-foreground">
            {dataQuality.map((item) => (
              <div key={item.label} className="rounded-md border border-border bg-panel-subtle p-2">
                <div className="text-[11px] uppercase">{item.label}</div>
                <div className="text-foreground">{item.value}</div>
              </div>
            ))}
          </div>
        </Card>
      </div>

      <Card className="border-border bg-panel">
        <div className="flex items-center justify-between border-b border-border px-4 py-3">
          <div>
            <div className="text-sm font-semibold text-foreground">
              Версии датасетов
            </div>
            <div className="text-xs text-muted-foreground">
              Версионированные датасеты с хешами пайплайнов.
            </div>
          </div>
          <Button size="sm">
            <PlugZap className="mr-2 h-4 w-4" />
            Использовать в бэктесте
          </Button>
        </div>
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
      </Card>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <ChartCard title="Предпросмотр таблицы" subtitle="Упрощенный OHLC и объем">
          <OhlcPreviewChart />
        </ChartCard>
        <Card className="border-border bg-panel lg:col-span-2">
          <div className="border-b border-border px-4 py-3">
            <div className="text-sm font-semibold text-foreground">Таблица предпросмотра</div>
            <div className="text-xs text-muted-foreground">
              Первые строки из датасета "Акции США v13".
            </div>
          </div>
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
        </Card>
      </div>
    </div>
  );
}
