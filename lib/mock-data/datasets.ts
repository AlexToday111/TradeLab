export type DataSource = {
  id: string;
  name: string;
  type: "Exchange" | "API" | "CSV" | "S3";
  status: "connected" | "idle";
};

export type DatasetVersion = {
  id: string;
  name: string;
  period: string;
  timeframe: string;
  symbols: string[];
  size: string;
  pipelineHash: string;
};

export const dataSources: DataSource[] = [
  { id: "src-1", name: "Binance Spot", type: "Exchange", status: "connected" },
  { id: "src-2", name: "Polygon.io", type: "API", status: "connected" },
  { id: "src-3", name: "Локальный CSV", type: "CSV", status: "idle" },
  { id: "src-4", name: "S3-бакеты", type: "S3", status: "connected" },
];

export const datasetVersions: DatasetVersion[] = [
  {
    id: "v13",
    name: "Акции США v13",
    period: "2018-01-01 -> 2024-12-31",
    timeframe: "1D",
    symbols: ["SPY", "QQQ", "IWM", "EFA", "EEM"],
    size: "14.2 GB",
    pipelineHash: "pl_7c91",
  },
  {
    id: "v12",
    name: "Акции США v12",
    period: "2016-01-01 -> 2023-12-31",
    timeframe: "1D",
    symbols: ["SPY", "QQQ", "IWM", "EFA", "EEM"],
    size: "12.4 GB",
    pipelineHash: "pl_5a22",
  },
  {
    id: "v21",
    name: "ETF внутри дня v21",
    period: "2022-01-01 -> 2024-12-31",
    timeframe: "15m",
    symbols: ["SPY", "QQQ", "TLT", "GLD"],
    size: "28.8 GB",
    pipelineHash: "pl_2d11",
  },
];

export const pipelineSteps = [
  "загрузка",
  "очистка",
  "ресемплинг",
  "признаки",
  "сохранение",
];

export const dataQuality = [
  { label: "Покрытие", value: "98.7%" },
  { label: "Пропуски", value: "0.6%" },
  { label: "Дубликаты", value: "0.2%" },
  { label: "Выбросы", value: "0.5%" },
];

export const previewRows = [
  { ts: "2024-01-02", open: 485.2, high: 489.4, low: 482.1, close: 487.8, volume: "62.1M" },
  { ts: "2024-01-03", open: 487.9, high: 490.2, low: 483.4, close: 484.6, volume: "58.7M" },
  { ts: "2024-01-04", open: 484.5, high: 492.3, low: 483.8, close: 491.7, volume: "61.9M" },
  { ts: "2024-01-05", open: 491.6, high: 493.1, low: 488.2, close: 489.3, volume: "56.3M" },
  { ts: "2024-01-08", open: 489.4, high: 495.2, low: 489.1, close: 493.8, volume: "60.4M" },
];
