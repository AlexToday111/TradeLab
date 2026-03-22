"use client";

import { useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { Download, Filter, Repeat, CheckSquare } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { PageHeader } from "@/components/shared/page-header";
import { SurfaceCard } from "@/components/shared/surface-card";
import { RunsTable } from "@/features/runs/components/runs-table";
import { useRuns } from "@/features/runs/store/run-store";
import { getNextRunIds } from "@/lib/run-id";
import type { Run, RunStatus } from "@/lib/types";

const zeroMetrics = {
  pnl: 0,
  sharpe: 0,
  maxDrawdown: 0,
  trades: 0,
  winrate: 0,
  avgTrade: 0,
  feesImpact: 0,
};

function formatNowAsRunDate() {
  const now = new Date();
  const yyyy = now.getFullYear();
  const mm = String(now.getMonth() + 1).padStart(2, "0");
  const dd = String(now.getDate()).padStart(2, "0");
  const hh = String(now.getHours()).padStart(2, "0");
  const min = String(now.getMinutes()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd} ${hh}:${min}`;
}

function escapeCsv(value: string) {
  return `"${value.replace(/"/g, "\"\"")}"`;
}

function buildRerun(sourceRun: Run, id: string): Run {
  return {
    ...sourceRun,
    id,
    status: "queued",
    createdAt: formatNowAsRunDate(),
    artifacts: [],
    metrics: zeroMetrics,
    tags: Array.from(new Set([...sourceRun.tags, "candidate"])),
  };
}

function parseRunDate(value: string) {
  const parsed = Date.parse(value.replace(" ", "T"));
  return Number.isNaN(parsed) ? 0 : parsed;
}

export default function BacktestsPage() {
  const router = useRouter();
  const { runs, addRun } = useRuns();

  const [selected, setSelected] = useState<string[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState<"all" | RunStatus>("all");
  const [tagFilter, setTagFilter] = useState<"any" | "baseline" | "candidate" | "prod-like">(
    "any"
  );
  const [timeframeFilter, setTimeframeFilter] = useState<string>("all");

  const availableTimeframes = useMemo(
    () => Array.from(new Set(runs.map((run) => run.timeframe))).sort(),
    [runs]
  );

  const filteredRuns = useMemo(() => {
    const query = searchQuery.trim().toLowerCase();

    return runs
      .filter((run) => {
        const statusMatch = statusFilter === "all" || run.status === statusFilter;
        const tagMatch = tagFilter === "any" || run.tags.includes(tagFilter);
        const timeframeMatch = timeframeFilter === "all" || run.timeframe === timeframeFilter;
        const searchMatch =
          query.length === 0 ||
          run.id.toLowerCase().includes(query) ||
          run.strategy.toLowerCase().includes(query) ||
          run.datasetVersion.toLowerCase().includes(query);

        return statusMatch && tagMatch && timeframeMatch && searchMatch;
      })
      .sort((a, b) => parseRunDate(b.createdAt) - parseRunDate(a.createdAt));
  }, [runs, searchQuery, statusFilter, tagFilter, timeframeFilter]);

  const filteredIds = useMemo(() => filteredRuns.map((run) => run.id), [filteredRuns]);
  const selectedVisibleIds = selected.filter((id) => filteredIds.includes(id));
  const selectedVisibleCount = selectedVisibleIds.length;
  const allVisibleSelected =
    filteredIds.length > 0 && filteredIds.every((id) => selected.includes(id));

  const statusStats = useMemo(() => {
    return {
      total: filteredRuns.length,
      queued: filteredRuns.filter((run) => run.status === "queued").length,
      running: filteredRuns.filter((run) => run.status === "running").length,
      done: filteredRuns.filter((run) => run.status === "done").length,
      failed: filteredRuns.filter((run) => run.status === "failed").length,
    };
  }, [filteredRuns]);

  const toggleRun = (id: string) => {
    setSelected((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const toggleVisibleSelection = () => {
    if (allVisibleSelected) {
      setSelected((prev) => prev.filter((id) => !filteredIds.includes(id)));
      return;
    }
    setSelected((prev) => Array.from(new Set([...prev, ...filteredIds])));
  };

  const handleBulkRerun = () => {
    const selectedRuns = runs.filter((run) => selectedVisibleIds.includes(run.id));
    const nextIds = getNextRunIds(
      runs.map((run) => run.id),
      selectedRuns.length
    );
    selectedRuns.forEach((run, index) => addRun(buildRerun(run, nextIds[index])));
    setSelected((prev) => prev.filter((id) => !selectedVisibleIds.includes(id)));
  };

  const handleBulkExport = () => {
    const selectedRuns = runs.filter((run) => selectedVisibleIds.includes(run.id));
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
        eyebrow="Запуски"
        title="Бэктесты"
        description="Фильтруйте прогоны, быстро выбирайте нужные запуски и применяйте массовые действия."
        actions={
          <>
            <Button size="sm" variant="secondary" onClick={toggleVisibleSelection}>
              <CheckSquare className="mr-2 h-4 w-4" />
              {allVisibleSelected ? "Снять выбор" : "Выбрать по фильтру"}
            </Button>
            <Button
              size="sm"
              variant="secondary"
              onClick={handleBulkRerun}
              disabled={selectedVisibleCount === 0}
            >
              <Repeat className="mr-2 h-4 w-4" />
              Повторный запуск
            </Button>
            <Button size="sm" onClick={handleBulkExport} disabled={selectedVisibleCount === 0}>
              <Download className="mr-2 h-4 w-4" />
              Экспорт выбранных
            </Button>
          </>
        }
      />

      <div className="grid grid-cols-2 gap-3 md:grid-cols-5">
        <SurfaceCard className="py-0" contentClassName="p-3">
          <div className="text-[11px] uppercase text-muted-foreground">Всего</div>
          <div className="text-lg font-semibold text-foreground">{statusStats.total}</div>
        </SurfaceCard>
        <SurfaceCard className="py-0" contentClassName="p-3">
          <div className="text-[11px] uppercase text-muted-foreground">В очереди</div>
          <div className="text-lg font-semibold text-status-warning">{statusStats.queued}</div>
        </SurfaceCard>
        <SurfaceCard className="py-0" contentClassName="p-3">
          <div className="text-[11px] uppercase text-muted-foreground">Выполняется</div>
          <div className="text-lg font-semibold text-status-running">{statusStats.running}</div>
        </SurfaceCard>
        <SurfaceCard className="py-0" contentClassName="p-3">
          <div className="text-[11px] uppercase text-muted-foreground">Завершено</div>
          <div className="text-lg font-semibold text-status-success">{statusStats.done}</div>
        </SurfaceCard>
        <SurfaceCard className="py-0" contentClassName="p-3">
          <div className="text-[11px] uppercase text-muted-foreground">Ошибки</div>
          <div className="text-lg font-semibold text-status-failed">{statusStats.failed}</div>
        </SurfaceCard>
      </div>

      <SurfaceCard
        className="bg-[linear-gradient(130deg,rgba(29,41,79,0.34),rgba(19,24,35,0.95)_72%)]"
      >
        <div className="flex flex-wrap items-center gap-2">
          <div className="flex items-center gap-2 text-xs text-muted-foreground">
            <Filter className="h-4 w-4" />
            Фильтры
          </div>
          <Input
            className="h-8 w-[220px] text-xs"
            placeholder="ID, стратегия, датасет"
            value={searchQuery}
            onChange={(event) => setSearchQuery(event.target.value)}
          />
          <Select value={statusFilter} onValueChange={(value) => setStatusFilter(value as "all" | RunStatus)}>
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
          <Select value={tagFilter} onValueChange={(value) => setTagFilter(value as "any" | "baseline" | "candidate" | "prod-like")}>
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
              setTagFilter("any");
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
            onRowClick={(id) => router.push(`/runs/${id}`)}
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
