"use client";

import { FolderOpen, Plus, UploadCloud } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { projects } from "@/lib/mock-data/projects";
import { datasetVersions } from "@/lib/mock-data/datasets";
import { useRuns } from "@/components/run/run-store";
import { RunStatusBadge } from "@/components/run/run-badges";
import { EmptyState } from "@/components/layout/empty-state";

export default function WorkspacePage() {
  const { runs } = useRuns();
  const recentRuns = runs.slice(0, 5);

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center justify-between">
        <div>
          <div className="text-lg font-semibold text-foreground">Рабочая область</div>
          <div className="text-xs text-muted-foreground">
            Проекты, датасеты и последние запуски в одном месте.
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button size="sm">
            <Plus className="mr-2 h-4 w-4" />
            Новый проект
          </Button>
          <Button size="sm" variant="secondary">
            <UploadCloud className="mr-2 h-4 w-4" />
            Импорт репозитория
          </Button>
          <Button size="sm" variant="secondary">
            <FolderOpen className="mr-2 h-4 w-4" />
            Открыть проект
          </Button>
        </div>
      </div>

      <Card className="border-border bg-panel">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Проект</TableHead>
              <TableHead>Описание</TableHead>
              <TableHead>Последний датасет</TableHead>
              <TableHead>Последние запуски</TableHead>
              <TableHead>Последняя активность</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {projects.map((project) => (
              <TableRow key={project.id}>
                <TableCell className="font-medium text-foreground">
                  {project.name}
                </TableCell>
                <TableCell className="text-xs text-muted-foreground">
                  {project.description}
                </TableCell>
                <TableCell className="text-xs text-foreground">
                  {project.lastDataset}
                </TableCell>
                <TableCell className="text-xs text-muted-foreground">
                  {project.recentRuns.join(", ")}
                </TableCell>
                <TableCell className="text-xs text-muted-foreground">
                  {project.lastActive}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Card>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <Card className="border-border bg-panel p-4">
          <div className="mb-3 text-sm font-semibold text-foreground">
            Закрепленные датасеты
          </div>
          <div className="flex flex-col gap-2">
            {datasetVersions.slice(0, 2).map((dataset) => (
              <div
                key={dataset.id}
                className="rounded-md border border-border bg-panel-subtle p-3 text-xs"
              >
                <div className="font-medium text-foreground">{dataset.name}</div>
                <div className="text-muted-foreground">
                  {dataset.period} / {dataset.timeframe} / {dataset.symbols.join(", ")}
                </div>
              </div>
            ))}
          </div>
        </Card>
        <Card className="border-border bg-panel p-4">
          <div className="mb-3 text-sm font-semibold text-foreground">
            Последние запуски
          </div>
          <div className="flex flex-col gap-2">
            {recentRuns.length === 0 ? (
              <EmptyState
                title="Запусков пока нет"
                description="Запустите первый бэктест, чтобы заполнить этот список."
                actionLabel="Запустить бэктест"
                actionHref="/code"
              />
            ) : (
              recentRuns.map((run) => (
                <div
                  key={run.id}
                  className="flex items-center justify-between rounded-md border border-border bg-panel-subtle p-3 text-xs"
                >
                  <div>
                    <div className="font-mono text-foreground">{run.id}</div>
                    <div className="text-muted-foreground">
                      {run.strategy} / {run.datasetVersion}
                    </div>
                  </div>
                  <RunStatusBadge status={run.status} />
                </div>
              ))
            )}
          </div>
        </Card>
      </div>
    </div>
  );
}
