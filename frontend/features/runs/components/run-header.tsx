"use client";

import { Run } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Download, Copy, FileText, Upload } from "lucide-react";
import { SurfaceCard } from "@/components/shared/surface-card";

export function RunHeader({ run }: { run: Run }) {
  return (
    <SurfaceCard contentClassName="p-5">
      <div className="flex flex-col items-center gap-4 text-center">
        <div className="flex w-full flex-wrap items-center justify-center gap-3">
          <div className="flex flex-wrap items-center justify-center gap-2">
            <Button size="sm" variant="secondary">
              <Copy className="mr-2 h-4 w-4" />
              Клонировать конфиг
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
        <div className="grid w-full grid-cols-2 gap-4 text-center text-xs text-muted-foreground md:grid-cols-4">
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
    </SurfaceCard>
  );
}
