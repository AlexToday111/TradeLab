"use client";

import { Run } from "@/lib/types";
import { RunDiffIndicators, RunStatusBadge } from "@/components/run/run-badges";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Download, GitCompare, Copy, Repeat2, FileText, Upload } from "lucide-react";

export function RunHeader({ run }: { run: Run }) {
  return (
    <div className="flex flex-col gap-3 rounded-lg border border-border bg-panel px-4 py-3">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <div>
            <div className="text-xs text-muted-foreground">ID запуска</div>
            <div className="font-mono text-sm text-foreground">{run.id}</div>
          </div>
          <RunStatusBadge status={run.status} />
          <RunDiffIndicators diff={run.diff} />
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <Button size="sm" variant="secondary">
            <Repeat2 className="mr-2 h-4 w-4" />
            Повторить запуск
          </Button>
          <Button size="sm" variant="secondary">
            <Copy className="mr-2 h-4 w-4" />
            Клонировать конфиг
          </Button>
          <Button size="sm" variant="secondary">
            <GitCompare className="mr-2 h-4 w-4" />
            Diff с...
          </Button>
          <Button size="sm" variant="secondary">
            <FileText className="mr-2 h-4 w-4" />
            Открыть код на коммите
          </Button>
          <Button size="sm" variant="secondary">
            <FileText className="mr-2 h-4 w-4" />
            Открыть версию датасета
          </Button>
          <Button size="sm">
            <Download className="mr-2 h-4 w-4" />
            Экспорт
          </Button>
          <Button size="sm" variant="secondary">
            <Upload className="mr-2 h-4 w-4" />
            Импорт
          </Button>
        </div>
      </div>
      <Separator />
      <div className="grid grid-cols-2 gap-4 text-xs text-muted-foreground md:grid-cols-4">
        <div>
          <div>Стратегия</div>
          <div className="text-foreground">{run.strategy}</div>
        </div>
        <div>
          <div>Датасет</div>
          <div className="text-foreground">{run.datasetVersion}</div>
        </div>
        <div>
          <div>Период</div>
          <div className="text-foreground">{run.period}</div>
        </div>
        <div>
          <div>Таймфрейм</div>
          <div className="text-foreground">{run.timeframe}</div>
        </div>
      </div>
    </div>
  );
}
