"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Filter, Download, Repeat } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { PageHeader } from "@/components/shared/page-header";
import { SurfaceCard } from "@/components/shared/surface-card";
import { RunsTable } from "@/features/runs/components/runs-table";
import { useRuns } from "@/features/runs/store/run-store";

export default function BacktestsPage() {
  const router = useRouter();
  const { runs } = useRuns();
  const [selected, setSelected] = useState<string[]>([]);

  const toggleRun = (id: string) => {
    setSelected((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  return (
    <div className="flex h-full flex-col gap-5">
      <PageHeader
        eyebrow="Запуски"
        title="Бэктесты"
        description="Очередь запусков, фильтрация и работа с результатами."
        actions={
          <>
            <Button size="sm" variant="secondary">
              <Repeat className="mr-2 h-4 w-4" />
              Повторный запуск
            </Button>
            <Button size="sm">
              <Download className="mr-2 h-4 w-4" />
              Массовый экспорт
            </Button>
          </>
        }
      />

      <SurfaceCard>
        <div className="flex flex-wrap items-center gap-2">
          <div className="flex items-center gap-2 text-xs text-muted-foreground">
            <Filter className="h-4 w-4" />
            Фильтры
          </div>
          <Input className="h-8 w-[200px] text-xs" placeholder="Поиск запусков" />
          <Select defaultValue="all">
            <SelectTrigger className="h-8 w-[160px] text-xs">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Все статусы</SelectItem>
              <SelectItem value="queued">В очереди</SelectItem>
              <SelectItem value="running">Выполняется</SelectItem>
              <SelectItem value="done">Завершен</SelectItem>
              <SelectItem value="failed">Ошибка</SelectItem>
            </SelectContent>
          </Select>
          <Select defaultValue="any">
            <SelectTrigger className="h-8 w-[160px] text-xs">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="any">Любой тег</SelectItem>
              <SelectItem value="baseline">Базовый</SelectItem>
              <SelectItem value="candidate">Кандидат</SelectItem>
              <SelectItem value="prod-like">Как в проде</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </SurfaceCard>

      <SurfaceCard contentClassName="p-0">
        <RunsTable
          runs={runs}
          selectedIds={selected}
          onToggle={toggleRun}
          onRowClick={(id) => router.push(`/runs/${id}`)}
        />
      </SurfaceCard>
    </div>
  );
}
