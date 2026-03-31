"use client";

import { useEffect, useMemo, useRef, useState, type ChangeEvent } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import {
  Activity,
  Database,
  Download,
  FolderInput,
  Play,
  Plus,
  SlidersHorizontal,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ChartCard } from "@/components/shared/chart-card";
import { PageHeader } from "@/components/shared/page-header";
import { SurfaceCard } from "@/components/shared/surface-card";
import { DrawdownChart, EquityChart } from "@/features/runs/charts/run-charts";
import { useRuns } from "@/features/runs/store/run-store";
import { uploadStrategy } from "@/lib/api/strategies";
import { projects, type Project } from "@/lib/demo-data/projects";
import { getProjectRuns } from "@/lib/project-runs";

const DEFAULT_START_BALANCE_USD = 100_000;
type UploadState = "idle" | "uploading" | "success" | "error";

function parseRunDate(value: string) {
  const parsed = Date.parse(value.replace(" ", "T"));
  return Number.isNaN(parsed) ? 0 : parsed;
}

function formatUsdAmount(value: number) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0,
  }).format(value);
}

function formatStrategyName(strategy: string) {
  const normalized = strategy.replace(/\.py$/i, "").replace(/_/g, " ").trim();
  return normalized.replace(/\b\w/g, (char) => char.toUpperCase());
}

function formatNowAsProjectTimestamp() {
  return new Date().toISOString().slice(0, 16).replace("T", " ");
}

function createProjectId(name: string) {
  const slug = name
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
  return `proj-${slug || "custom"}-${Math.random().toString(36).slice(2, 6)}`;
}

function toErrorMessage(error: unknown, fallback: string) {
  if (error instanceof Error && error.message.trim().length > 0) {
    return error.message;
  }
  return fallback;
}

export default function DesktopPage() {
  const { runs } = useRuns();
  const searchParams = useSearchParams();
  const requestedProjectId = searchParams.get("project");
  const strategyFileInputRef = useRef<HTMLInputElement | null>(null);

  const [projectOptions, setProjectOptions] = useState<Project[]>(projects);
  const [selectedProjectId, setSelectedProjectId] = useState<string | null>(null);
  const [isCreateProjectOpen, setIsCreateProjectOpen] = useState(false);
  const [newProjectName, setNewProjectName] = useState("");
  const [newProjectDescription, setNewProjectDescription] = useState("");
  const [isAddStrategyOpen, setIsAddStrategyOpen] = useState(false);
  const [newStrategyName, setNewStrategyName] = useState("");
  const [uploadState, setUploadState] = useState<UploadState>("idle");
  const [uploadError, setUploadError] = useState<string | null>(null);

  useEffect(() => {
    if (!requestedProjectId) {
      return;
    }

    if (projectOptions.some((project) => project.id === requestedProjectId)) {
      setSelectedProjectId(requestedProjectId);
    }
  }, [projectOptions, requestedProjectId]);

  const project = useMemo(() => {
    if (!selectedProjectId) {
      return null;
    }
    return projectOptions.find((item) => item.id === selectedProjectId) ?? null;
  }, [projectOptions, selectedProjectId]);

  const projectSelectionItems = useMemo(() => {
    return projectOptions.map((item) => {
      const itemRuns = getProjectRuns(runs, item.id);
      const averagePnl =
        itemRuns.length === 0
          ? null
          : itemRuns.reduce((total, run) => total + run.metrics.pnl, 0) / itemRuns.length;

      return {
        project: item,
        runCount: itemRuns.length,
        averagePnl,
      };
    });
  }, [projectOptions, runs]);

  const projectRuns = useMemo(() => {
    if (!project) {
      return [];
    }

    return getProjectRuns(runs, project.id).sort(
      (a, b) => parseRunDate(b.createdAt) - parseRunDate(a.createdAt)
    );
  }, [project, runs]);

  const recentProjectRuns = projectRuns.slice(0, 5);
  const primaryRun = projectRuns[0] ?? null;

  const isProjectProfitable = primaryRun ? primaryRun.metrics.pnl > 0 : false;
  const desktopSurfaceToneClassName = isProjectProfitable
    ? "border-[rgba(93,187,99,0.28)] bg-[linear-gradient(180deg,rgba(93,187,99,0.14),rgba(11,15,24,0.93)_24%,rgba(11,15,24,0.96))] shadow-[inset_0_1px_0_rgba(255,255,255,0.08),0_24px_56px_rgba(0,0,0,0.42),0_0_0_1px_rgba(93,187,99,0.16)]"
    : "border-[rgba(179,0,0,0.34)] bg-[linear-gradient(180deg,rgba(179,0,0,0.14),rgba(11,15,24,0.93)_24%,rgba(11,15,24,0.96))] shadow-[inset_0_1px_0_rgba(255,255,255,0.08),0_24px_56px_rgba(0,0,0,0.42),0_0_0_1px_rgba(179,0,0,0.22)]";

  const projectMetrics = [
    {
      label: "Последний датасет",
      description: "Текущая версия данных для основного сценария",
      value: project?.lastDataset ?? "n/a",
      actionTitle: "Подготовить файлы стратегии",
      actionDescription:
        "В дальнейшем сюда будет подключен сабмит файлов и проверка их структуры.",
      actionIcon: FolderInput,
      accent: "#31D633",
      glow: "rgba(49, 214, 51, 0.32)",
    },
    {
      label: "Sharpe / Max Drawdown",
      description: "Ключевые метрики качества последнего run",
      value: primaryRun
        ? `Sharpe ${primaryRun.metrics.sharpe.toFixed(2)} / Max DD ${primaryRun.metrics.maxDrawdown.toFixed(1)}%`
        : "n/a",
      actionTitle: "Настроить параметры проекта",
      actionDescription: "Комиссии, датасет, версия стратегии и пресеты запуска.",
      actionIcon: SlidersHorizontal,
      accent: "#33CC99",
      glow: "rgba(51, 204, 153, 0.3)",
    },
    {
      label: "Последняя активность",
      description: "Когда проект обновлялся в последний раз",
      value: project?.lastActive ?? "n/a",
      actionTitle: "Экспортировать артефакты",
      actionDescription: "Отчеты, результаты запусков и связанные файлы проекта.",
      actionIcon: Download,
      accent: "#5D9548",
      glow: "rgba(93, 149, 72, 0.3)",
    },
  ];

  const canCreateProject = newProjectName.trim().length > 0;
  const canAddStrategy = newStrategyName.trim().length > 0;

  const handleCreateProject = () => {
    if (!canCreateProject) {
      return;
    }

    const newProject: Project = {
      id: createProjectId(newProjectName),
      name: newProjectName.trim(),
      description: newProjectDescription.trim() || "Новый проект для дальнейшей настройки.",
      lastDataset: "Не выбран",
      lastRunId: "run_new",
      lastActive: formatNowAsProjectTimestamp(),
      recentRuns: [],
    };

    setProjectOptions((current) => [newProject, ...current]);
    setSelectedProjectId(newProject.id);
    setNewProjectName("");
    setNewProjectDescription("");
    setIsCreateProjectOpen(false);
  };

  const handleStrategyPickerOpen = () => {
    if (uploadState === "uploading") {
      return;
    }

    strategyFileInputRef.current?.click();
  };

  const handleAddStrategy = () => {
    setIsAddStrategyOpen(false);
    setNewStrategyName("");
  };

  const handleStrategyFileSelect = async (event: ChangeEvent<HTMLInputElement>) => {
    const selectedFile = event.target.files?.[0];
    event.target.value = "";

    if (!selectedFile) {
      return;
    }

    if (!selectedFile.name.toLowerCase().endsWith(".py")) {
      setUploadState("error");
      setUploadError("Разрешены только файлы с расширением .py");
      return;
    }

    setUploadState("uploading");
    setUploadError(null);

    try {
      await uploadStrategy(selectedFile);
      setUploadState("success");
    } catch (error) {
      setUploadState("error");
      setUploadError(toErrorMessage(error, "Загрузка стратегии завершилась с ошибкой."));
    }
  };

  if (!project) {
    return (
      <div className="flex h-full flex-col gap-5">
        <SurfaceCard className="border-white/12 bg-[linear-gradient(140deg,rgba(16,22,32,0.96),rgba(9,13,21,0.94))]">
          <div className="space-y-4">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <div className="text-2xl font-semibold tracking-tight text-foreground">
                  Выберите проект
                </div>
                <div className="mt-1 text-sm text-muted-foreground">
                  Откройте существующий проект или создайте новый.
                </div>
              </div>
              <Button
                size="sm"
                className="w-fit"
                onClick={() => setIsCreateProjectOpen((current) => !current)}
              >
                <Plus className="mr-2 h-4 w-4" />
                Добавить проект
              </Button>
            </div>

            {isCreateProjectOpen ? (
              <div className="rounded-[18px] border border-white/12 bg-[rgba(8,12,20,0.68)] p-4">
                <div className="grid gap-3 lg:grid-cols-[minmax(0,1fr)_220px]">
                  <div className="space-y-3">
                    <input
                      value={newProjectName}
                      onChange={(event) => setNewProjectName(event.target.value)}
                      placeholder="Название проекта"
                      className="h-10 w-full rounded-[12px] border border-white/12 bg-[rgba(14,18,28,0.82)] px-3 text-sm text-foreground placeholder:text-muted-foreground/80 outline-none transition-colors focus:border-[hsl(var(--tl-glow)/0.5)]"
                    />
                    <textarea
                      value={newProjectDescription}
                      onChange={(event) => setNewProjectDescription(event.target.value)}
                      placeholder="Краткое описание проекта"
                      rows={3}
                      className="w-full resize-none rounded-[12px] border border-white/12 bg-[rgba(14,18,28,0.82)] px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground/80 outline-none transition-colors focus:border-[hsl(var(--tl-glow)/0.5)]"
                    />
                  </div>
                  <div className="flex items-end gap-2 lg:flex-col lg:justify-end">
                    <Button
                      size="sm"
                      className="w-full"
                      disabled={!canCreateProject}
                      onClick={handleCreateProject}
                    >
                      Создать
                    </Button>
                    <Button
                      size="sm"
                      variant="secondary"
                      className="w-full"
                      onClick={() => setIsCreateProjectOpen(false)}
                    >
                      Отмена
                    </Button>
                  </div>
                </div>
              </div>
            ) : null}

            <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
              {projectSelectionItems.map((item) => {
                const isProfit = item.averagePnl !== null && item.averagePnl >= 0;

                return (
                  <div
                    key={item.project.id}
                    role="button"
                    tabIndex={0}
                    className="group rounded-[20px] border border-white/12 bg-[linear-gradient(155deg,rgba(20,27,38,0.95),rgba(9,13,21,0.95))] p-4 transition-all duration-300 hover:-translate-y-0.5 hover:border-white/20 hover:shadow-[0_16px_32px_rgba(0,0,0,0.36)]"
                    onClick={() => setSelectedProjectId(item.project.id)}
                    onKeyDown={(event) => {
                      if (event.key === "Enter" || event.key === " ") {
                        event.preventDefault();
                        setSelectedProjectId(item.project.id);
                      }
                    }}
                  >
                    <div className="text-base font-semibold text-foreground">
                      {item.project.name}
                    </div>
                    <div className="mt-1 min-h-[36px] text-xs leading-relaxed text-muted-foreground">
                      {item.project.description}
                    </div>
                    <div className="mt-3 grid grid-cols-2 gap-2 text-xs">
                      <div className="rounded-[12px] border border-white/10 bg-[rgba(3,6,12,0.46)] px-2.5 py-2">
                        <div className="text-[10px] uppercase tracking-[0.12em] text-muted-foreground">
                          Датасет
                        </div>
                        <div className="mt-1 text-foreground">{item.project.lastDataset}</div>
                      </div>
                      <div className="rounded-[12px] border border-white/10 bg-[rgba(3,6,12,0.46)] px-2.5 py-2">
                        <div className="text-[10px] uppercase tracking-[0.12em] text-muted-foreground">
                          Запуски
                        </div>
                        <div className="mt-1 text-foreground">{item.runCount}</div>
                      </div>
                    </div>
                    <div className="mt-3 flex items-center justify-between">
                      <div className="text-[11px] text-muted-foreground">
                        Ср. PnL:{" "}
                        <span
                          className={
                            item.averagePnl === null
                              ? "text-muted-foreground"
                              : isProfit
                                ? "font-medium text-status-success"
                                : "font-medium text-status-failed"
                          }
                        >
                          {item.averagePnl === null
                            ? "n/a"
                            : `${item.averagePnl >= 0 ? "+" : ""}${item.averagePnl.toFixed(1)}%`}
                        </span>
                      </div>
                      <div className="rounded-full border border-white/12 px-2.5 py-1 text-[11px] text-foreground/90">
                        Открыть
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </SurfaceCard>
      </div>
    );
  }

  return (
    <div className="flex h-full flex-col gap-5">
      <PageHeader
        title="Рабочий стол"
        actions={
          <>
            <Select value={project.id} onValueChange={setSelectedProjectId}>
              <SelectTrigger className="h-9 w-[240px] border-white/15 bg-[#0F141E] text-xs">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {projectOptions.map((item) => (
                  <SelectItem key={item.id} value={item.id}>
                    {item.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Button size="sm">
              <Play className="mr-2 h-4 w-4" />
              Запустить сценарий
            </Button>
            <Button
              size="sm"
              variant="secondary"
              onClick={handleStrategyPickerOpen}
              disabled={uploadState === "uploading"}
            >
              <Plus className="mr-2 h-4 w-4" />
              Добавить стратегию
            </Button>
            <Button asChild size="sm" variant="secondary">
              <Link href={`/backtests?project=${project.id}`}>Все бэктесты проекта</Link>
            </Button>
          </>
        }
      />

      <SurfaceCard className={desktopSurfaceToneClassName}>
        <div className="space-y-4">
          <div className="space-y-3">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <div className="text-2xl font-semibold tracking-tight text-foreground">
                  {project.name}
                </div>
                <div className="mt-2 max-w-2xl text-sm text-muted-foreground">
                  {project.description}
                </div>
              </div>
            </div>

            <input
              ref={strategyFileInputRef}
              type="file"
              accept=".py"
              className="hidden"
              onChange={handleStrategyFileSelect}
            />
            <div className="text-xs text-muted-foreground">
              {uploadState === "uploading" ? "Файл загружается и валидируется..." : null}
              {uploadState === "success" ? "Стратегия успешно загружена." : null}
              {uploadState === "error" ? uploadError : null}
            </div>

            {isAddStrategyOpen ? (
              <div className="rounded-[18px] border border-white/12 bg-[rgba(8,12,20,0.68)] p-4">
                <div className="grid gap-3 lg:grid-cols-[minmax(0,1fr)_220px]">
                  <input
                    value={newStrategyName}
                    onChange={(event) => setNewStrategyName(event.target.value)}
                    placeholder="Имя стратегии (например, atlas_breakout.py)"
                    className="h-10 w-full rounded-[12px] border border-white/12 bg-[rgba(14,18,28,0.82)] px-3 text-sm text-foreground placeholder:text-muted-foreground/80 outline-none transition-colors focus:border-[hsl(var(--tl-glow)/0.5)]"
                  />
                  <div className="flex items-end gap-2 lg:flex-col lg:justify-end">
                    <Button
                      size="sm"
                      className="w-full"
                      disabled={!canAddStrategy}
                      onClick={handleAddStrategy}
                    >
                      Добавить
                    </Button>
                    <Button
                      size="sm"
                      variant="secondary"
                      className="w-full"
                      onClick={() => setIsAddStrategyOpen(false)}
                    >
                      Отмена
                    </Button>
                  </div>
                </div>
              </div>
            ) : null}
          </div>
          <div className="grid gap-3 md:grid-cols-3">
            {projectMetrics.map((metric) => {
              const ActionIcon = metric.actionIcon;

              return (
                <div key={metric.label} className="space-y-3">
                  <div className="group relative overflow-hidden rounded-[22px] border border-white/15 bg-[linear-gradient(145deg,rgba(34,39,51,0.96),rgba(16,20,30,0.94))] p-4 shadow-[0_14px_36px_rgba(0,0,0,0.34)]">
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
                  <div className="rounded-[16px] border border-white/12 bg-[rgba(8,11,18,0.46)] px-3 py-3">
                    <div className="mb-1 flex items-center gap-2 text-xs font-medium text-white/90">
                      <ActionIcon className="h-3.5 w-3.5" />
                      {metric.actionTitle}
                    </div>
                    <div className="text-[11px] leading-relaxed text-white/62">
                      {metric.actionDescription}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </SurfaceCard>

      <div className="overflow-hidden rounded-[18px] border border-white/10 bg-[#0F141E]">
        <div className="overflow-x-auto">
          <table className="min-w-[700px] w-full text-xs">
            <thead className="bg-[linear-gradient(135deg,hsl(var(--tl-glow)/0.22),hsl(var(--tl-glow-soft)/0.16))] text-foreground/90">
              <tr className="border-b border-white/10">
                <th className="px-4 py-3 text-left text-[11px] font-medium uppercase tracking-[0.14em]">
                  Название
                </th>
                <th className="px-4 py-3 text-left text-[11px] font-medium uppercase tracking-[0.14em]">
                  Датасет
                </th>
                <th className="px-4 py-3 text-left text-[11px] font-medium uppercase tracking-[0.14em]">
                  Прибыль/убыток
                </th>
                <th className="px-4 py-3 text-left text-[11px] font-medium uppercase tracking-[0.14em]">
                  Баланс до/после
                </th>
                <th className="px-4 py-3 text-right text-[11px] font-medium uppercase tracking-[0.14em]">
                  Посмотреть всё
                </th>
              </tr>
            </thead>
            <tbody>
              {recentProjectRuns.length === 0 ? (
                <tr>
                  <td
                    colSpan={5}
                    className="px-4 py-5 text-center text-xs text-muted-foreground"
                  >
                    Для этого проекта пока нет запусков.
                  </td>
                </tr>
              ) : (
                recentProjectRuns.map((run) => {
                  const isProfit = run.metrics.pnl >= 0;
                  const balanceBefore = DEFAULT_START_BALANCE_USD;
                  const balanceAfter =
                    DEFAULT_START_BALANCE_USD * (1 + run.metrics.pnl / 100);

                  return (
                    <tr key={run.id} className="border-b border-white/10 last:border-b-0">
                      <td className="px-4 py-3 align-middle">
                        <div className="font-medium text-foreground">
                          {formatStrategyName(run.strategy)}
                        </div>
                        <div className="mt-1 font-mono text-[11px] text-muted-foreground">
                          {run.id}
                        </div>
                      </td>
                      <td className="px-4 py-3 align-middle text-muted-foreground">
                        {run.datasetVersion}
                      </td>
                      <td className="px-4 py-3 align-middle">
                        <span
                          className={
                            isProfit
                              ? "font-semibold text-status-success"
                              : "font-semibold text-status-failed"
                          }
                        >
                          {isProfit ? "+" : ""}
                          {run.metrics.pnl.toFixed(1)}%
                        </span>
                      </td>
                      <td className="px-4 py-3 align-middle text-muted-foreground">
                        {formatUsdAmount(balanceBefore)} {"\u2192"}{" "}
                        {formatUsdAmount(balanceAfter)}
                      </td>
                      <td className="px-4 py-3 align-middle text-right">
                        <Button
                          asChild
                          size="sm"
                          variant="secondary"
                          className="h-7 rounded-full px-3"
                        >
                          <Link href={`/runs/${run.id}`}>Открыть</Link>
                        </Button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="grid gap-4 xl:grid-cols-[minmax(0,1.35fr)_360px]">
        <div className="grid gap-4 lg:grid-cols-2">
          <ChartCard title="Динамика капитала">
            <EquityChart />
          </ChartCard>
          <ChartCard title="Текущая просадка">
            <DrawdownChart />
          </ChartCard>
        </div>

        <SurfaceCard title="Контекст проекта">
          <div className="space-y-3 text-xs">
            <div className="rounded-[18px] border border-border bg-panel-subtle p-4">
              <div className="mb-1 flex items-center gap-2 text-foreground">
                <Database className="h-4 w-4" />
                Датасет
              </div>
              <div className="text-muted-foreground">{project.lastDataset}</div>
            </div>
            <div className="rounded-[18px] border border-border bg-panel-subtle p-4">
              <div className="mb-1 flex items-center gap-2 text-foreground">
                <Activity className="h-4 w-4" />
                Будущие манипуляции
              </div>
              <div className="text-muted-foreground">
                Загрузка файлов, подбор параметров, история версий, запуск и сравнение
                сценариев.
              </div>
            </div>
            <div className="rounded-[18px] border border-dashed border-border bg-panel-subtle p-4 text-muted-foreground">
              Здесь позже появятся таймлайн проекта, список файлов, артефакты и
              кастомные виджеты.
            </div>
          </div>
        </SurfaceCard>
      </div>
    </div>
  );
}
