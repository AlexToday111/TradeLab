"use client";

import {
  Activity,
  Database,
  Download,
  FolderInput,
  GitCompare,
  Play,
  SlidersHorizontal,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ChartCard } from "@/components/shared/chart-card";
import { PageHeader } from "@/components/shared/page-header";
import { SurfaceCard } from "@/components/shared/surface-card";
import { DrawdownChart, EquityChart } from "@/features/runs/charts/run-charts";
import { MetricCard } from "@/features/runs/components/metric-card";
import { RunStatusBadge } from "@/features/runs/components/run-badges";
import { useRuns } from "@/features/runs/store/run-store";
import { datasetVersions } from "@/lib/demo-data/datasets";
import { projects } from "@/lib/demo-data/projects";

export default function DesktopPage() {
  const { runs } = useRuns();
  const project = projects[0];
  const primaryRun = runs[0];
  const dataset = datasetVersions[0];

  return (
    <div className="flex h-full flex-col gap-5">
      <PageHeader
        eyebrow="Рабочий стол проекта"
        title="Рабочий стол"
        description={`Выбран проект ${project.name}. Здесь будет основной экран управления проектом, запусков и загружаемых файлов.`}
        actions={
          <>
            <Button size="sm">
              <FolderInput className="mr-2 h-4 w-4" />
              Загрузить файлы
            </Button>
            <Button size="sm" variant="secondary">
              <Play className="mr-2 h-4 w-4" />
              Запустить сценарий
            </Button>
            <Button size="sm" variant="secondary">
              <GitCompare className="mr-2 h-4 w-4" />
              Сравнить
            </Button>
          </>
        }
      />

      <SurfaceCard>
        <div className="grid gap-4 lg:grid-cols-[minmax(0,1.4fr)_360px]">
          <div className="space-y-4">
            <div className="flex flex-wrap gap-2">
              <Badge variant="secondary">{project.lastDataset}</Badge>
              <Badge variant="secondary">{project.lastRunId}</Badge>
              {primaryRun ? <RunStatusBadge status={primaryRun.status} /> : null}
            </div>
            <div>
              <div className="text-2xl font-semibold tracking-tight text-foreground">
                {project.name}
              </div>
              <div className="mt-2 max-w-2xl text-sm text-muted-foreground">
                {project.description}
              </div>
            </div>
            <div className="grid gap-3 md:grid-cols-3">
              <MetricCard label="Последний датасет" value={project.lastDataset} />
              <MetricCard label="Последний запуск" value={project.lastRunId} />
              <MetricCard label="Последняя активность" value={project.lastActive} />
            </div>
          </div>
          <div className="rounded-[22px] border border-border bg-panel-subtle p-4">
            <div className="mb-3 text-sm font-semibold text-foreground">
              Ближайшие действия
            </div>
            <div className="space-y-3 text-xs">
              <div className="rounded-[18px] border border-border bg-panel px-3 py-3">
                <div className="mb-1 flex items-center gap-2 text-foreground">
                  <FolderInput className="h-4 w-4" />
                  Подготовить файлы стратегии
                </div>
                <div className="text-muted-foreground">
                  В дальнейшем сюда будет подключен сабмит файлов и проверка их структуры.
                </div>
              </div>
              <div className="rounded-[18px] border border-border bg-panel px-3 py-3">
                <div className="mb-1 flex items-center gap-2 text-foreground">
                  <SlidersHorizontal className="h-4 w-4" />
                  Настроить параметры проекта
                </div>
                <div className="text-muted-foreground">
                  Комиссии, датасет, версия стратегии и пресеты запуска.
                </div>
              </div>
              <div className="rounded-[18px] border border-border bg-panel px-3 py-3">
                <div className="mb-1 flex items-center gap-2 text-foreground">
                  <Download className="h-4 w-4" />
                  Экспортировать артефакты
                </div>
                <div className="text-muted-foreground">
                  Отчеты, результаты запусков и связанные файлы проекта.
                </div>
              </div>
            </div>
          </div>
        </div>
      </SurfaceCard>

      <div className="grid gap-4 xl:grid-cols-[minmax(0,1.35fr)_360px]">
        <div className="grid gap-4 lg:grid-cols-2">
          <ChartCard
            title="Динамика капитала"
            subtitle="Базовая визуализация выбранного проекта"
          >
            <EquityChart />
          </ChartCard>
          <ChartCard
            title="Текущая просадка"
            subtitle="Контроль устойчивости сценария"
          >
            <DrawdownChart />
          </ChartCard>
        </div>

        <SurfaceCard
          title="Контекст проекта"
          subtitle="То, что будет расширяться на рабочем столе"
        >
          <div className="space-y-3 text-xs">
            <div className="rounded-[18px] border border-border bg-panel-subtle p-4">
              <div className="mb-1 flex items-center gap-2 text-foreground">
                <Database className="h-4 w-4" />
                Датасет
              </div>
              <div className="text-muted-foreground">
                {dataset.name} / {dataset.period}
              </div>
            </div>
            <div className="rounded-[18px] border border-border bg-panel-subtle p-4">
              <div className="mb-1 flex items-center gap-2 text-foreground">
                <Activity className="h-4 w-4" />
                Будущие манипуляции
              </div>
              <div className="text-muted-foreground">
                Загрузка файлов, подбор параметров, история версий, запуск и сравнение сценариев.
              </div>
            </div>
            <div className="rounded-[18px] border border-dashed border-border bg-panel-subtle p-4 text-muted-foreground">
              Здесь позже появятся таймлайн проекта, список файлов, артефакты и кастомные виджеты.
            </div>
          </div>
        </SurfaceCard>
      </div>
    </div>
  );
}
