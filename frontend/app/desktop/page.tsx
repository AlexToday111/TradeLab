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
import { ChartCard } from "@/components/shared/chart-card";
import { PageHeader } from "@/components/shared/page-header";
import { SurfaceCard } from "@/components/shared/surface-card";
import { DrawdownChart, EquityChart } from "@/features/runs/charts/run-charts";
import { useRuns } from "@/features/runs/store/run-store";
import { datasetVersions } from "@/lib/demo-data/datasets";
import { projects } from "@/lib/demo-data/projects";

export default function DesktopPage() {
  const { runs } = useRuns();
  const project = projects[0];
  const primaryRun = runs[0];
  const dataset = datasetVersions[0];
  const isProjectProfitable = primaryRun ? primaryRun.metrics.pnl > 0 : false;
  const desktopSurfaceToneClassName = isProjectProfitable
    ? "border-[rgba(93,187,99,0.28)] bg-[linear-gradient(180deg,rgba(93,187,99,0.14),rgba(11,15,24,0.93)_24%,rgba(11,15,24,0.96))] shadow-[inset_0_1px_0_rgba(255,255,255,0.08),0_24px_56px_rgba(0,0,0,0.42),0_0_0_1px_rgba(93,187,99,0.16)]"
    : "border-[rgba(179,0,0,0.34)] bg-[linear-gradient(180deg,rgba(179,0,0,0.14),rgba(11,15,24,0.93)_24%,rgba(11,15,24,0.96))] shadow-[inset_0_1px_0_rgba(255,255,255,0.08),0_24px_56px_rgba(0,0,0,0.42),0_0_0_1px_rgba(179,0,0,0.22)]";
  const projectMetrics = [
    {
      label: "Последний датасет",
      description: "Текущая версия данных для основного сценария",
      value: project.lastDataset,
      accent: "#31D633",
      glow: "rgba(49, 214, 51, 0.32)",
    },
    {
      label: "Sharpe / Max Drawdown",
      description: "Ключевые метрики качества последнего run",
      value: primaryRun
        ? `Sharpe ${primaryRun.metrics.sharpe.toFixed(2)} / Max DD ${primaryRun.metrics.maxDrawdown.toFixed(1)}%`
        : "n/a",
      accent: "#33CC99",
      glow: "rgba(51, 204, 153, 0.3)",
    },
    {
      label: "Последняя активность",
      description: "Когда проект обновлялся в последний раз",
      value: project.lastActive,
      accent: "#5D9548",
      glow: "rgba(93, 149, 72, 0.3)",
    },
  ];

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

      <SurfaceCard className={desktopSurfaceToneClassName}>
        <div className="grid gap-4 lg:grid-cols-[minmax(0,1.4fr)_360px]">
          <div className="space-y-4">
            <div>
              <div className="text-2xl font-semibold tracking-tight text-foreground">
                {project.name}
              </div>
              <div className="mt-2 max-w-2xl text-sm text-muted-foreground">
                {project.description}
              </div>
            </div>
            <div className="grid gap-3 md:grid-cols-3">
              {projectMetrics.map((metric) => (
                <div
                  key={metric.label}
                  className="group relative overflow-hidden rounded-[22px] border border-white/15 bg-[linear-gradient(145deg,rgba(34,39,51,0.96),rgba(16,20,30,0.94))] p-4 shadow-[0_14px_36px_rgba(0,0,0,0.34)]"
                >
                  <div
                    className="pointer-events-none absolute inset-y-0 left-0 w-4"
                    style={{
                      background: `linear-gradient(180deg, ${metric.accent} 0%, rgba(255, 255, 255, 0.08) 100%)`,
                    }}
                  />
                  <div
                    className="pointer-events-none absolute -left-8 top-1/2 h-24 w-24 -translate-y-1/2 rounded-full blur-2xl"
                    style={{ backgroundColor: metric.glow }}
                  />
                  <div className="relative pl-4">
                    <div className="text-lg font-semibold leading-tight text-white">
                      {metric.label}
                    </div>
                    <div className="mt-2 text-xs leading-relaxed text-white/65">
                      {metric.description}
                    </div>
                    <div className="mt-4 text-sm font-medium text-white/90">
                      {metric.value}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
          <div className="rounded-[22px] border border-border bg-panel-subtle p-4 text-center">
            <div className="mb-3 text-sm font-semibold text-foreground">
              Возможные действия
            </div>
            <div className="space-y-3 text-xs">
              <div className="rounded-[18px] border border-border bg-panel px-3 py-3">
                <div className="mb-1 flex items-center justify-center gap-2 text-foreground">
                  <FolderInput className="h-4 w-4" />
                  Подготовить файлы стратегии
                </div>
                <div className="text-muted-foreground">
                  В дальнейшем сюда будет подключен сабмит файлов и проверка их структуры.
                </div>
              </div>
              <div className="rounded-[18px] border border-border bg-panel px-3 py-3">
                <div className="mb-1 flex items-center justify-center gap-2 text-foreground">
                  <SlidersHorizontal className="h-4 w-4" />
                  Настроить параметры проекта
                </div>
                <div className="text-muted-foreground">
                  Комиссии, датасет, версия стратегии и пресеты запуска.
                </div>
              </div>
              <div className="rounded-[18px] border border-border bg-panel px-3 py-3">
                <div className="mb-1 flex items-center justify-center gap-2 text-foreground">
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
