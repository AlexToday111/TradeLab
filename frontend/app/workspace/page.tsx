"use client";

import {
  Activity,
  ArrowUpRight,
  Database,
  FolderKanban,
  Sparkles,
} from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { EmptyState } from "@/components/shared/empty-state";
import { SurfaceCard } from "@/components/shared/surface-card";
import { RunStatusBadge } from "@/features/runs/components/run-badges";
import { useRuns } from "@/features/runs/store/run-store";
import { datasetVersions } from "@/lib/demo-data/datasets";
import { projects } from "@/lib/demo-data/projects";

export default function WorkspacePage() {
  const { runs } = useRuns();
  const recentRuns = runs.slice(0, 5);
  const runningRuns = runs.filter((run) => run.status === "running").length;
  const queuedRuns = runs.filter((run) => run.status === "queued").length;

  const summaryCards = [
    {
      label: "Проекты",
      value: projects.length,
      hint: "активные рабочие пространства",
      icon: FolderKanban,
    },
    {
      label: "Датасеты",
      value: datasetVersions.length,
      hint: "версии, готовые к запуску",
      icon: Database,
    },
    {
      label: "Запуски",
      value: runs.length,
      hint: `${runningRuns} выполняется, ${queuedRuns} в очереди`,
      icon: Activity,
    },
  ];

  return (
    <div className="flex h-full flex-col gap-5">

      <div className="grid gap-4 xl:grid-cols-[minmax(0,1.8fr)_340px]">
        <SurfaceCard
          title="Активные проекты"
          subtitle="Короткий обзор текущих направлений"
        >
          <div className="grid gap-4 lg:grid-cols-2">
            {projects.map((project, index) => (
              <div
                key={project.id}
                className="group overflow-hidden rounded-[22px] border border-border bg-panel-subtle"
              >
                <div className="border-b border-border/80 bg-[linear-gradient(135deg,rgba(106,161,255,0.14),rgba(20,24,35,0)_72%)] px-4 py-4">
                  <div className="mb-3 flex items-start justify-between gap-3">
                    <div>
                      <div className="text-lg font-semibold text-foreground">
                        {project.name}
                      </div>
                      <div className="mt-1 text-sm text-muted-foreground">
                        {project.description}
                      </div>
                    </div>
                    <Badge variant="secondary">
                      #{(index + 1).toString().padStart(2, "0")}
                    </Badge>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    <Badge variant="secondary">{project.lastDataset}</Badge>
                    <Badge variant="secondary">{project.lastRunId}</Badge>
                  </div>
                </div>
                <div className="space-y-4 px-4 py-4">
                  <div className="grid grid-cols-2 gap-3">
                    <div className="rounded-xl border border-border bg-panel px-3 py-3 text-xs">
                      <div className="mb-1 text-[11px] uppercase tracking-[0.18em] text-muted-foreground">
                        Последняя активность
                      </div>
                      <div className="text-foreground">{project.lastActive}</div>
                    </div>
                    <div className="rounded-xl border border-border bg-panel px-3 py-3 text-xs">
                      <div className="mb-1 text-[11px] uppercase tracking-[0.18em] text-muted-foreground">
                        Запуски
                      </div>
                      <div className="text-foreground">{project.recentRuns.length}</div>
                    </div>
                  </div>
                  <div className="flex items-center justify-between text-xs text-muted-foreground">
                    <div>Готов к открытию на рабочем столе</div>
                    <div className="flex items-center gap-1 text-foreground transition group-hover:text-primary">
                      Открыть
                      <ArrowUpRight className="h-3.5 w-3.5" />
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </SurfaceCard>

        <div className="grid gap-4">
          {summaryCards.map((card) => {
            const Icon = card.icon;
            return (
              <SurfaceCard key={card.label}>
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <div className="text-[11px] uppercase tracking-[0.2em] text-muted-foreground">
                      {card.label}
                    </div>
                    <div className="mt-3 text-3xl font-semibold text-foreground">
                      {card.value}
                    </div>
                    <div className="mt-2 text-xs text-muted-foreground">
                      {card.hint}
                    </div>
                  </div>
                  <div className="rounded-xl border border-border bg-panel-subtle p-2.5">
                    <Icon className="h-4 w-4 text-muted-foreground" />
                  </div>
                </div>
              </SurfaceCard>
            );
          })}
        </div>
      </div>

      <div className="grid gap-4">
        <SurfaceCard title="Последние запуски" subtitle="Что происходило последним">
          <div className="flex flex-col gap-3">
            {recentRuns.length === 0 ? (
              <EmptyState
                title="Запусков пока нет"
                description="Запустите первый бэктест, чтобы заполнить этот список."
                actionLabel="Открыть рабочий стол"
                actionHref="/desktop"
              />
            ) : (
              recentRuns.map((run) => (
                <div
                  key={run.id}
                  className="flex items-center justify-between rounded-[18px] border border-border bg-panel-subtle p-4 text-xs"
                >
                  <div>
                    <div className="font-mono text-foreground">{run.id}</div>
                    <div className="mt-1 text-muted-foreground">
                      {run.strategy} / {run.datasetVersion}
                    </div>
                  </div>
                  <RunStatusBadge status={run.status} />
                </div>
              ))
            )}
          </div>
        </SurfaceCard>

      </div>
    </div>
  );
}
