"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import Image from "next/image";
import {
  ChevronLeft,
  ChevronRight,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/shared/empty-state";
import { SurfaceCard } from "@/components/shared/surface-card";
import { useRuns } from "@/features/runs/store/run-store";
import { datasetVersions } from "@/lib/demo-data/datasets";
import { projects } from "@/lib/demo-data/projects";

const CHART_WIDTH = 560;
const CHART_HEIGHT = 260;
const DEFAULT_START_BALANCE_USD = 100_000;

// TODO: replace with real timeseries from backend once API is connected.
const projectChartPlaceholders: Record<string, number[]> = {
  "proj-atlas": [24, 30, 28, 36, 33, 42, 48, 45, 52, 58],
  "proj-orbit": [38, 34, 29, 33, 31, 36, 41, 39, 44, 47],
  "proj-ridge": [20, 23, 27, 25, 30, 34, 32, 37, 40, 43],
};

function buildSparklinePath(values: number[], width: number, height: number) {
  if (values.length === 0) {
    return "";
  }

  const max = Math.max(...values);
  const min = Math.min(...values);
  const range = max - min || 1;
  const stepX = width / Math.max(values.length - 1, 1);

  return values
    .map((value, index) => {
      const x = index * stepX;
      const y = height - ((value - min) / range) * (height - 18) - 10;
      return `${index === 0 ? "M" : "L"}${x.toFixed(2)} ${y.toFixed(2)}`;
    })
    .join(" ");
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

export default function WorkspacePage() {
  const { runs } = useRuns();
  const recentRuns = runs.slice(0, 5);
  const runningRuns = runs.filter((run) => run.status === "running").length;
  const queuedRuns = runs.filter((run) => run.status === "queued").length;
  const [projectPage, setProjectPage] = useState(0);
  const [projectsPerView, setProjectsPerView] = useState(2);
  const projectTones = [
    {
      accent: "#33CC99",
      glow: "rgba(51, 204, 153, 0.32)",
      tint: "rgba(51, 204, 153, 0.16)",
    },
    {
      accent: "#6AA1FF",
      glow: "rgba(106, 161, 255, 0.3)",
      tint: "rgba(106, 161, 255, 0.16)",
    },
    {
      accent: "#E5A84A",
      glow: "rgba(229, 168, 74, 0.3)",
      tint: "rgba(229, 168, 74, 0.14)",
    },
  ];

  const summaryCards = [
    {
      label: "Проекты",
      value: projects.length,
      hint: "активные рабочие пространства",
      iconSrc: "/icons/Module-Puzzle-3--Streamline-Core.svg",
      iconAlt: "Иконка проектов",
    },
    {
      label: "Датасеты",
      value: datasetVersions.length,
      hint: "версии, готовые к запуску",
      iconSrc: "/icons/Database-Server-1--Streamline-Core.svg",
      iconAlt: "Иконка датасетов",
    },
    {
      label: "Запуски",
      value: runs.length,
      hint: `${runningRuns} выполняется, ${queuedRuns} в очереди`,
      iconSrc: "/icons/Desktop-Check--Streamline-Core.svg",
      iconAlt: "Иконка запусков",
    },
  ];
  const fxRates = [
    { pair: "USD/RUB", value: "90.24" },
    { pair: "EUR/RUB", value: "98.17" },
    { pair: "GBP/RUB", value: "115.84" },
    { pair: "CNY/RUB", value: "12.49" },
  ];

  useEffect(() => {
    const updateProjectsPerView = () => {
      setProjectsPerView(window.innerWidth < 1024 ? 1 : 2);
    };

    updateProjectsPerView();
    window.addEventListener("resize", updateProjectsPerView);
    return () => window.removeEventListener("resize", updateProjectsPerView);
  }, []);

  const maxProjectPage = Math.max(0, projects.length - projectsPerView);
  const canGoPrev = projectPage > 0;
  const canGoNext = projectPage < maxProjectPage;

  useEffect(() => {
    setProjectPage((current) => Math.min(current, maxProjectPage));
  }, [maxProjectPage]);

  const translateX = useMemo(
    () => `translateX(-${(projectPage * 100) / projectsPerView}%)`,
    [projectPage, projectsPerView]
  );

  return (
    <div className="flex h-full flex-col gap-5">
      <SurfaceCard>
        <div className="relative">
          {canGoPrev ? (
            <Button
              variant="ghost"
              size="icon"
              className="absolute left-0 top-1/2 z-20 h-11 w-11 -translate-x-1/2 -translate-y-1/2 rounded-full border-0 bg-transparent text-foreground/70 shadow-none hover:bg-transparent hover:text-foreground"
              onClick={() => setProjectPage((current) => Math.max(0, current - 1))}
            >
              <ChevronLeft className="h-5 w-5" />
            </Button>
          ) : null}
          {canGoNext ? (
            <Button
              variant="ghost"
              size="icon"
              className="absolute right-0 top-1/2 z-20 h-11 w-11 translate-x-1/2 -translate-y-1/2 rounded-full border-0 bg-transparent text-foreground/70 shadow-none hover:bg-transparent hover:text-foreground"
              onClick={() =>
                setProjectPage((current) => Math.min(maxProjectPage, current + 1))
              }
            >
              <ChevronRight className="h-5 w-5" />
            </Button>
          ) : null}

          <div className="overflow-hidden">
          <div
            className="flex transition-transform duration-500 ease-out"
            style={{ transform: translateX }}
          >
            {projects.map((project, index) => {
              const tone = projectTones[index % projectTones.length];
              const chartValues = projectChartPlaceholders[project.id] ?? [22, 26, 24, 31, 29, 35, 38, 41];
              const chartPath = buildSparklinePath(chartValues, CHART_WIDTH, CHART_HEIGHT);
              const chartAreaPath = chartPath
                ? `${chartPath} L ${CHART_WIDTH} ${CHART_HEIGHT} L 0 ${CHART_HEIGHT} Z`
                : "";

              return (
                <div
                  key={project.id}
                  className="shrink-0 px-2 first:pl-0 last:pr-0"
                  style={{ flex: `0 0 ${100 / projectsPerView}%` }}
                >
                  <div className="group relative flex min-h-[430px] flex-col overflow-hidden rounded-[24px] border border-white/10 bg-[linear-gradient(150deg,rgba(21,27,39,0.96),rgba(9,13,22,0.95)_78%)] transition-all duration-300 hover:-translate-y-0.5 hover:border-white/20 hover:shadow-[0_22px_44px_rgba(0,0,0,0.45)]">
                    <div
                      className="pointer-events-none absolute inset-0 opacity-0 transition-opacity duration-300 group-hover:opacity-100"
                      style={{
                        background: `radial-gradient(560px 220px at -6% 18%, ${tone.tint}, rgba(20,24,35,0) 62%)`,
                      }}
                    />
                    <div className="pointer-events-none absolute inset-0 z-0 opacity-45">
                      <svg
                        viewBox={`0 0 ${CHART_WIDTH} ${CHART_HEIGHT}`}
                        preserveAspectRatio="none"
                        className="h-full w-full"
                      >
                        <defs>
                          <linearGradient id={`project-chart-fill-${project.id}`} x1="0" y1="0" x2="0" y2="1">
                            <stop offset="0%" stopColor={`${tone.accent}55`} />
                            <stop offset="100%" stopColor={`${tone.accent}00`} />
                          </linearGradient>
                        </defs>
                        <path d={`M 0 78 H ${CHART_WIDTH}`} stroke="rgba(255,255,255,0.06)" strokeDasharray="5 6" />
                        <path d={`M 0 152 H ${CHART_WIDTH}`} stroke="rgba(255,255,255,0.05)" strokeDasharray="5 6" />
                        <path d={chartAreaPath} fill={`url(#project-chart-fill-${project.id})`} />
                        <path
                          d={chartPath}
                          fill="none"
                          stroke={tone.accent}
                          strokeWidth="2.6"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          opacity="0.9"
                        />
                      </svg>
                    </div>
                    <div className="pointer-events-none absolute right-4 top-3 z-0 select-none text-[82px] font-semibold leading-none text-white/[0.08]">
                      {(index + 1).toString().padStart(2, "0")}
                    </div>
                    <div className="relative z-10 border-b border-white/10 px-4 py-4">
                      <div
                        className="pointer-events-none absolute inset-y-0 left-0 w-1"
                        style={{ backgroundColor: tone.accent }}
                      />
                      <div
                        className="pointer-events-none absolute -left-10 top-1/2 h-28 w-28 -translate-y-1/2 rounded-full blur-[28px]"
                        style={{ backgroundColor: tone.glow }}
                      />
                      <div className="relative mb-3 pl-3 pr-14">
                        <div>
                          <div className="text-lg font-semibold leading-tight text-foreground">
                            {project.name}
                          </div>
                          <div className="mt-1 text-sm text-muted-foreground">
                            {project.description}
                          </div>
                        </div>
                      </div>
                    </div>
                    <div className="relative z-10 flex flex-1 flex-col px-4 pb-0 pt-4">
                      <div className="grid grid-cols-2 gap-3">
                        <div className="flex flex-col gap-3">
                          <div className="rounded-xl border border-white/10 bg-white/[0.03] px-3 py-3 text-xs">
                            <div className="mb-1 text-[11px] uppercase tracking-[0.18em] text-muted-foreground">
                              Последняя активность
                            </div>
                            <div className="text-foreground">{project.lastActive}</div>
                          </div>
                          <div className="rounded-xl border border-white/10 bg-white/[0.03] px-3 py-3 text-xs">
                            <div className="mb-1 text-[11px] uppercase tracking-[0.18em] text-muted-foreground">
                              Датасет
                            </div>
                            <div className="text-foreground">{project.lastDataset}</div>
                          </div>
                        </div>
                        <div className="self-start rounded-xl border border-white/10 bg-white/[0.03] px-3 py-3 text-xs">
                          <div className="mb-1 text-[11px] uppercase tracking-[0.18em] text-muted-foreground">
                            Запуски
                          </div>
                          <div className="text-foreground">{project.recentRuns.length}</div>
                        </div>
                      </div>
                      <div className="mt-auto pt-4">
                        <Button
                          variant="secondary"
                          className="-mx-4 h-11 w-[calc(100%+2rem)] rounded-none rounded-b-[24px] border-0 text-foreground/90 hover:brightness-105"
                          style={{
                            background: `linear-gradient(135deg, ${tone.accent}44, ${tone.accent}22)`,
                            boxShadow: `inset 0 1px 0 rgba(255,255,255,0.06), 0 -8px 18px ${tone.accent}1a`,
                          }}
                        >
                          Открыть
                        </Button>
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
          </div>
        </div>
      </SurfaceCard>

      <div className="rounded-[28px] border border-white/10 p-2">
        <div className="grid gap-2 md:grid-cols-3">
          {summaryCards.map((card) => {
            return (
              <SurfaceCard
                key={card.label}
                className="w-full rounded-[22px] border-white/10 bg-[#0F141E] shadow-none"
                contentClassName="p-0"
              >
                <div className="relative min-h-[112px] overflow-hidden">
                  <Image
                    src={card.iconSrc}
                    alt={card.iconAlt}
                    width={172}
                    height={172}
                    className="pointer-events-none absolute -left-[58px] top-1/2 h-[172px] w-[172px] -translate-y-1/2 select-none opacity-25"
                  />
                  <div className="pointer-events-none absolute inset-y-0 left-0 w-[46%] bg-[linear-gradient(90deg,rgba(10,14,22,0.08),rgba(10,14,22,0))]" />
                  <div className="relative ml-[34%] pl-3 pr-3 py-3">
                    <div className="text-[11px] uppercase tracking-[0.2em] text-muted-foreground">
                      {card.label}
                    </div>
                    <div className="mt-1 text-xs text-muted-foreground">
                      {card.hint}
                    </div>
                    <div className="mt-3 text-3xl font-semibold text-foreground">
                      {card.value}
                    </div>
                  </div>
                </div>
              </SurfaceCard>
            );
          })}
        </div>
      </div>

      {recentRuns.length === 0 ? (
        <EmptyState
          title="Запусков пока нет"
          description="Запустите первый бэктест, чтобы заполнить этот список."
          actionLabel="Открыть рабочий стол"
          actionHref="/desktop"
        />
      ) : (
        <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_300px]">
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
                  {recentRuns.map((run) => {
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
                          <div className="mt-1 font-mono text-[11px] text-muted-foreground">{run.id}</div>
                        </td>
                        <td className="px-4 py-3 align-middle text-muted-foreground">
                          {run.datasetVersion}
                        </td>
                        <td className="px-4 py-3 align-middle">
                          <span
                            className={isProfit ? "font-semibold text-status-success" : "font-semibold text-status-failed"}
                          >
                            {isProfit ? "+" : ""}
                            {run.metrics.pnl.toFixed(1)}%
                          </span>
                        </td>
                        <td className="px-4 py-3 align-middle text-muted-foreground">
                          {formatUsdAmount(balanceBefore)} {"\u2192"} {formatUsdAmount(balanceAfter)}
                        </td>
                        <td className="px-4 py-3 align-middle text-right">
                          <Button asChild size="sm" variant="secondary" className="h-7 rounded-full px-3">
                            <Link href={`/runs/${run.id}`}>Открыть</Link>
                          </Button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>

          <div className="rounded-[18px] border border-white/10 bg-[#0F141E] p-4">
            <div className="mb-2 border-b border-[hsl(var(--tl-glow)/0.45)] pb-2 text-[11px] uppercase tracking-[0.14em] text-muted-foreground">
              Курсы валют
            </div>
            <div className="mt-3 space-y-2 rounded-[14px] border border-[hsl(var(--tl-glow)/0.32)] bg-[linear-gradient(160deg,hsl(var(--tl-glow)/0.24),hsl(var(--tl-glow-soft)/0.12)_58%,rgba(7,9,15,0.82))] p-2">
              {fxRates.map((rate) => (
                <div
                  key={rate.pair}
                  className="flex items-center justify-between rounded-[12px] border border-white/10 bg-[rgba(3,5,10,0.46)] px-3 py-2"
                >
                  <div className="text-[11px] uppercase tracking-[0.1em] text-muted-foreground">
                    {rate.pair}
                  </div>
                  <div className="font-mono text-sm text-foreground">{rate.value}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
