"use client";

import { Suspense, useEffect, useMemo, useState } from "react";
import { useSearchParams } from "next/navigation";
import { Download } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { PageHeader } from "@/components/shared/page-header";
import { SurfaceCard } from "@/components/shared/surface-card";
import { RunsTable } from "@/features/runs/components/runs-table";
import { useRuns } from "@/features/runs/store/run-store";
import { projects } from "@/lib/demo-data/projects";
import { getRunProjectId } from "@/lib/project-runs";
import type { RunStatus } from "@/lib/types";

function escapeCsv(value: string) {
  return `"${value.replace(/"/g, "\"\"")}"`;
}

function parseRunDate(value: string) {
  const parsed = Date.parse(value.replace(" ", "T"));
  return Number.isNaN(parsed) ? 0 : parsed;
}

function BacktestsPageContent() {
  const searchParams = useSearchParams();
  const { runs, deleteRun } = useRuns();

  const [selected, setSelected] = useState<string[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState<"all" | RunStatus>("all");
  const [timeframeFilter, setTimeframeFilter] = useState<string>("all");
  const [projectFilter, setProjectFilter] = useState<string>("all");

  useEffect(() => {
    const projectFromQuery = searchParams.get("project");
    if (!projectFromQuery) {
      return;
    }

    if (projects.some((project) => project.id === projectFromQuery)) {
      setProjectFilter(projectFromQuery);
    }
  }, [searchParams]);

  const realRuns = useMemo(
    () => runs.filter((run) => typeof run.backendRunId === "number"),
    [runs]
  );

  const availableTimeframes = useMemo(
    () => Array.from(new Set(realRuns.map((run) => run.timeframe))).sort(),
    [realRuns]
  );

  const filteredRuns = useMemo(() => {
    const query = searchQuery.trim().toLowerCase();

    return realRuns
      .filter((run) => {
        const statusMatch = statusFilter === "all" || run.status === statusFilter;
        const timeframeMatch = timeframeFilter === "all" || run.timeframe === timeframeFilter;
        const projectMatch =
          projectFilter === "all" || getRunProjectId(run) === projectFilter;
        const searchMatch =
          query.length === 0 ||
          run.id.toLowerCase().includes(query) ||
          run.strategy.toLowerCase().includes(query) ||
          run.datasetVersion.toLowerCase().includes(query);

        return statusMatch && timeframeMatch && projectMatch && searchMatch;
      })
      .sort((a, b) => parseRunDate(b.createdAt) - parseRunDate(a.createdAt));
  }, [projectFilter, realRuns, searchQuery, statusFilter, timeframeFilter]);

  const filteredIds = useMemo(() => filteredRuns.map((run) => run.id), [filteredRuns]);
  const selectedVisibleIds = selected.filter((id) => filteredIds.includes(id));
  const selectedVisibleCount = selectedVisibleIds.length;

  const statusStats = useMemo(
    () => ({
      total: filteredRuns.length,
      queued: filteredRuns.filter((run) => run.status === "queued").length,
      running: filteredRuns.filter((run) => run.status === "running").length,
      done: filteredRuns.filter((run) => run.status === "done").length,
      failed: filteredRuns.filter((run) => run.status === "failed").length,
    }),
    [filteredRuns]
  );

  const toggleRun = (id: string) => {
    setSelected((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const handleDeleteRun = (id: string) => {
    deleteRun(id);
    setSelected((prev) => prev.filter((item) => item !== id));
  };

  const handleBulkExport = () => {
    const selectedRuns = realRuns.filter((run) => selectedVisibleIds.includes(run.id));
    const header = ["id", "status", "strategy", "dataset", "timeframe", "period", "pnl", "sharpe"];
    const rows = selectedRuns.map((run) => [
      run.id,
      run.status,
      run.strategy,
      run.datasetVersion,
      run.timeframe,
      run.period,
      run.metrics.pnl.toFixed(2),
      run.metrics.sharpe.toFixed(2),
    ]);

    const payload = [header, ...rows]
      .map((row) => row.map((cell) => escapeCsv(cell)).join(","))
      .join("\n");

    const blob = new Blob([payload], { type: "text/csv;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `runs-export-${Date.now()}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="flex h-full flex-col gap-5">
      <PageHeader
        title="Бэктесты"
        actions={
          <Button size="sm" onClick={handleBulkExport} disabled={selectedVisibleCount === 0}>
            <Download className="mr-2 h-4 w-4" />
            Экспорт выбранных
          </Button>
        }
      />

      <div className="grid grid-cols-2 gap-3 md:grid-cols-5">
        <SurfaceCard className="py-0" contentClassName="p-3">
          <div className="text-center text-sm font-medium uppercase tracking-[0.08em] text-muted-foreground">
            Всего
          </div>
          <div className="text-right text-2xl font-semibold text-foreground">{statusStats.total}</div>
        </SurfaceCard>
        <SurfaceCard className="py-0" contentClassName="p-3">
          <div className="text-center text-sm font-medium uppercase tracking-[0.08em] text-muted-foreground">
            В очереди
          </div>
          <div className="text-right text-2xl font-semibold text-status-warning">{statusStats.queued}</div>
        </SurfaceCard>
        <SurfaceCard className="py-0" contentClassName="p-3">
          <div className="text-center text-sm font-medium uppercase tracking-[0.08em] text-muted-foreground">
            Выполняется
          </div>
          <div className="text-right text-2xl font-semibold text-status-running">{statusStats.running}</div>
        </SurfaceCard>
        <SurfaceCard className="py-0" contentClassName="p-3">
          <div className="text-center text-sm font-medium uppercase tracking-[0.08em] text-muted-foreground">
            Завершено
          </div>
          <div className="text-right text-2xl font-semibold text-status-success">{statusStats.done}</div>
        </SurfaceCard>
        <SurfaceCard className="py-0" contentClassName="p-3">
          <div className="text-center text-sm font-medium uppercase tracking-[0.08em] text-muted-foreground">
            Ошибки
          </div>
          <div className="text-right text-2xl font-semibold text-status-failed">{statusStats.failed}</div>
        </SurfaceCard>
      </div>

      <SurfaceCard
        className="bg-[linear-gradient(130deg,rgba(29,41,79,0.34),rgba(19,24,35,0.95)_72%)]"
      >
        <div className="flex flex-wrap items-center justify-center gap-2">
          <Input
            className="h-8 w-[220px] text-xs"
            placeholder="ID, стратегия, датасет"
            value={searchQuery}
            onChange={(event) => setSearchQuery(event.target.value)}
          />
          <Select
            value={statusFilter}
            onValueChange={(value) => setStatusFilter(value as "all" | RunStatus)}
          >
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
          <Select value={projectFilter} onValueChange={setProjectFilter}>
            <SelectTrigger className="h-8 w-[180px] text-xs">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Все проекты</SelectItem>
              {projects.map((project) => (
                <SelectItem key={project.id} value={project.id}>
                  {project.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={timeframeFilter} onValueChange={setTimeframeFilter}>
            <SelectTrigger className="h-8 w-[140px] text-xs">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Любой ТФ</SelectItem>
              {availableTimeframes.map((item) => (
                <SelectItem key={item} value={item}>
                  {item}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Button
            size="sm"
            variant="secondary"
            className="h-8 text-xs"
            onClick={() => {
              setSearchQuery("");
              setStatusFilter("all");
              setProjectFilter("all");
              setTimeframeFilter("all");
            }}
          >
            Сбросить
          </Button>
        </div>
      </SurfaceCard>

      <SurfaceCard
        contentClassName="p-0"
        title="Список запусков"
        subtitle={`Показано: ${filteredRuns.length}. Выбрано: ${selectedVisibleCount}.`}
      >
        {filteredRuns.length > 0 ? (
          <RunsTable
            runs={filteredRuns}
            selectedIds={selected}
            onToggle={toggleRun}
            onDelete={handleDeleteRun}
          />
        ) : (
          <div className="m-4 rounded-[14px] border border-dashed border-border bg-panel-subtle p-5 text-sm text-muted-foreground">
            По текущим фильтрам запусков не найдено.
          </div>
        )}
      </SurfaceCard>
    </div>
  );
}

export default function BacktestsPage() {
  return (
    <Suspense
      fallback={
        <div className="flex h-full items-center justify-center text-sm text-muted-foreground">
          Загрузка бэктестов...
        </div>
      }
    >
      <BacktestsPageContent />
    </Suspense>
  );
}
