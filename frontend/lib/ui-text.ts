import { RunStatus } from "@/lib/types";

const runStatusLabels: Record<RunStatus, string> = {
  queued: "В очереди",
  running: "Выполняется",
  done: "Завершен",
  failed: "Ошибка",
  canceled: "Отменен",
};

const runTagLabels = {
  baseline: "Базовый",
  candidate: "Кандидат",
  "prod-like": "Как в проде",
} as const;

const dataSourceTypeLabels = {
  Exchange: "Биржа",
  API: "API",
  CSV: "CSV",
  S3: "S3",
} as const;

const dataSourceStatusLabels = {
  connected: "Подключен",
  idle: "Неактивен",
} as const;

const tradeSideLabels = {
  Long: "Лонг",
  Short: "Шорт",
} as const;

const runDiffLabels = {
  code: "Код",
  data: "Данные",
  config: "Конфиг",
} as const;

export function getRunStatusLabel(status: RunStatus) {
  return runStatusLabels[status];
}

export function getRunTagLabel(tag: string) {
  return runTagLabels[tag as keyof typeof runTagLabels] ?? tag;
}

export function getDataSourceTypeLabel(type: string) {
  return dataSourceTypeLabels[type as keyof typeof dataSourceTypeLabels] ?? type;
}

export function getDataSourceStatusLabel(status: string) {
  return (
    dataSourceStatusLabels[status as keyof typeof dataSourceStatusLabels] ?? status
  );
}

export function getTradeSideLabel(side: string) {
  return tradeSideLabels[side as keyof typeof tradeSideLabels] ?? side;
}

export function getRunDiffLabel(key: "code" | "data" | "config") {
  return runDiffLabels[key];
}
