"use client";

import { useRouter } from "next/navigation";
import { Run } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Download, SlidersHorizontal, Upload } from "lucide-react";
import { SurfaceCard } from "@/components/shared/surface-card";

export function RunHeader({ run }: { run: Run }) {
  const router = useRouter();

  const handleOpenDatasetVersion = () => {
    const params = new URLSearchParams({ dataset: run.datasetVersion });
    router.push(`/data?${params.toString()}`);
  };

  return (
    <SurfaceCard contentClassName="p-5">
      <div className="flex flex-col items-center gap-4 text-center">
        <div className="w-full overflow-x-auto">
          <div className="flex min-w-[940px] items-center justify-between gap-4">
            <div className="flex flex-1 items-center justify-start gap-2">
              <Button size="sm" variant="secondary" className="shrink-0">
                Клонировать конфиг
              </Button>
              <Button
                size="sm"
                variant="secondary"
                className="shrink-0"
                onClick={handleOpenDatasetVersion}
              >
                Открыть версию датасета
              </Button>
            </div>
            <div className="flex shrink-0 justify-center">
              <Button size="sm" className="shrink-0">
                <SlidersHorizontal className="mr-2 h-4 w-4" />
                Настройка гиперпараметров
              </Button>
            </div>
            <div className="flex flex-1 items-center justify-end gap-2">
              <Button size="sm" variant="secondary" className="shrink-0">
                <Download className="mr-2 h-4 w-4" />
                Экспорт
              </Button>
              <Button size="sm" variant="secondary" className="shrink-0">
                <Upload className="mr-2 h-4 w-4" />
                Импорт
              </Button>
            </div>
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
