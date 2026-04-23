"use client";

import { ChangeEvent, useEffect, useMemo, useState } from "react";
import Image from "next/image";
import { Database, Download, Plus, UploadCloud } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { PageHeader } from "@/components/shared/page-header";
import { SurfaceCard } from "@/components/shared/surface-card";
import {
  dataSources,
  previewRows,
  type DatasetVersion,
} from "@/lib/demo-data/datasets";
import { apiFetch } from "@/lib/api/client";
import { cn } from "@/lib/utils";

type ExchangeSource = "binance" | "bybit" | "moex";
type DatasetSource = ExchangeSource | "local";
type MarketType = "spot" | "futures" | "shares";
type LocalCsvMode = "merge" | "separate";
type DatasetLoadStatus = "queued" | "processing" | "ready" | "error";
type BinanceLoadMode = "latest" | "range";
type UiTimeframe = "5M" | "1H" | "1D";

type CsvValidation = {
  isValid: boolean;
  detectedColumns: string[];
  missingColumns: string[];
  duplicateTimestampCount: number;
  missingValueCount: number;
  unsortedCount: number;
  totalRows: number;
  dateRangeStart: string | null;
  dateRangeEnd: string | null;
  inferredStep: string;
  symbolCoverage: string;
};

type CsvImportIssue = {
  fileName: string;
  rowNumber: number;
  timestamp: string;
  column: string;
  issue: "missing_column" | "missing_value" | "duplicate_timestamp" | "unsorted_timestamp";
  value: string;
};

type CsvValidationResult = {
  validation: CsvValidation | null;
  issues: CsvImportIssue[];
};

type DatasetProfile = {
  rowCount: string;
  dateRange: string;
  timeStep: string;
  symbolCoverage: string;
};

type BacktestCompatibility = {
  compatible: boolean;
  missingFields: string[];
  notes: string[];
};

type UiDataset = DatasetVersion & {
  source: DatasetSource;
  marketType: MarketType;
  loadStatus: DatasetLoadStatus;
  archived: boolean;
  profile: DatasetProfile;
  compatibility: BacktestCompatibility;
  tags: string[];
  rowsHint?: string;
  mergedCsvUrl?: string;
  mergedCsvName?: string;
  mergedCsvRows?: number;
  candleRows?: CandleTableRow[];
  backendRequest?: BackendRequestMeta;
  datasetVersion?: string;
  qualityStatus?: "OK" | "WARNING" | "FAILED";
  qualityIssueCount?: number;
  latestSnapshotId?: number;
};

type CandleTableRow = {
  ts: string;
  symbol: string;
  open: string;
  high: string;
  low: string;
  close: string;
  volume: string;
};

type BackendRequestMeta = {
  exchange: string;
  interval: string;
  from: string;
  to: string;
};

type ImportCandlesApiResponse = {
  status: string;
  exchange: string;
  symbol: string;
  interval: string;
  imported: number;
  from: string;
  to: string;
  dataset?: Record<string, unknown>;
};

type CandleApiResponse = {
  exchange: string;
  symbol: string;
  interval: string;
  openTime: string;
  closeTime: string;
  open: string;
  high: string;
  low: string;
  close: string;
  volume: string;
};

type PersistedDatasetPayload = Omit<
  UiDataset,
  "candleRows" | "mergedCsvUrl"
>;

type DatasetSnapshotApiResponse = {
  id: number;
  datasetId: string;
  datasetVersion: string;
  sourceExchange?: string | null;
  symbol?: string | null;
  timeframe?: string | null;
  startTime?: string | null;
  endTime?: string | null;
  rowCount?: number | null;
  checksum?: string | null;
  createdAt?: string | null;
};

type DatasetQualityApiResponse = {
  id: number;
  datasetId: string;
  snapshotId?: number | null;
  qualityStatus: "OK" | "WARNING" | "FAILED";
  issues?: unknown[];
  checkedAt?: string | null;
};

type MergedCsv = {
  url: string;
  name: string;
  rows: number;
  size: number;
};

type ImportTemplate = {
  id: string;
  name: string;
  source: DatasetSource;
  marketType: MarketType;
  symbolsInput: string;
  timeframe: UiTimeframe;
  binanceLoadMode: BinanceLoadMode;
  recentCandles: string;
  dateFrom: string;
  dateTo: string;
  localCsvMode: LocalCsvMode;
};

const sourceLabels: Record<DatasetSource, string> = {
  binance: "Binance",
  bybit: "Bybit",
  moex: "MOEX",
  local: "Локально",
};

const marketTypeLabels: Record<MarketType, string> = {
  spot: "Spot",
  futures: "Futures",
  shares: "Shares",
};

const exchangeTimeframes: Record<ExchangeSource, UiTimeframe[]> = {
  binance: ["5M", "1H", "1D"],
  bybit: ["5M", "1H", "1D"],
  moex: ["1H", "1D"],
};
const backendIntervalByTimeframe: Record<UiTimeframe, string> = {
  "5M": "5m",
  "1H": "1h",
  "1D": "1d",
};
const requiredCsvColumns = ["timestamp", "open", "high", "low", "close", "volume"];
const defaultImportTemplates: ImportTemplate[] = [
  {
    id: "tpl-binance-btc-1h",
    name: "Binance BTCUSDT 1H",
    source: "binance",
    marketType: "spot",
    symbolsInput: "BTCUSDT",
    timeframe: "1H",
    binanceLoadMode: "range",
    recentCandles: "500",
    dateFrom: "2025-01-01",
    dateTo: "2025-03-01",
    localCsvMode: "separate",
  },
  {
    id: "tpl-bybit-futures-5m",
    name: "Bybit Futures 5M",
    source: "bybit",
    marketType: "futures",
    symbolsInput: "BTCUSDT, ETHUSDT",
    timeframe: "5M",
    binanceLoadMode: "range",
    recentCandles: "2000",
    dateFrom: "2025-02-01",
    dateTo: "2025-03-01",
    localCsvMode: "separate",
  },
  {
    id: "tpl-moex-shares-1d",
    name: "MOEX Shares 1D",
    source: "moex",
    marketType: "shares",
    symbolsInput: "GAZP, SBER",
    timeframe: "1D",
    binanceLoadMode: "range",
    recentCandles: "250",
    dateFrom: "2025-01-01",
    dateTo: "2025-03-01",
    localCsvMode: "separate",
  },
  {
    id: "tpl-csv-merge",
    name: "CSV Merge Preset",
    source: "local",
    marketType: "spot",
    symbolsInput: "CSV",
    timeframe: "1H",
    binanceLoadMode: "range",
    recentCandles: "500",
    dateFrom: "",
    dateTo: "",
    localCsvMode: "merge",
  },
];

const loadStatusMeta: Record<
  DatasetLoadStatus,
  { label: string; className: string }
> = {
  queued: {
    label: "В очереди",
    className: "border border-status-warning/40 bg-status-warning/15 text-status-warning",
  },
  processing: {
    label: "Обрабатывается",
    className: "border border-status-running/40 bg-status-running/15 text-status-running",
  },
  ready: {
    label: "Готов",
    className: "border border-status-success/40 bg-status-success/15 text-status-success",
  },
  error: {
    label: "Ошибка",
    className: "border border-status-failed/40 bg-status-failed/15 text-status-failed",
  },
};

const importIssueLabels: Record<CsvImportIssue["issue"], string> = {
  missing_column: "Отсутствует колонка",
  missing_value: "Пустое значение",
  duplicate_timestamp: "Дубликат timestamp",
  unsorted_timestamp: "Нарушена сортировка",
};

function formatBytes(bytes: number) {
  if (!bytes) {
    return "0 B";
  }

  const units = ["B", "KB", "MB", "GB"];
  let value = bytes;
  let unitIndex = 0;

  while (value >= 1024 && unitIndex < units.length - 1) {
    value /= 1024;
    unitIndex += 1;
  }

  return `${value.toFixed(unitIndex === 0 ? 0 : 1)} ${units[unitIndex]}`;
}

function escapeCsvCell(value: string) {
  const normalized = value.replace(/"/g, "\"\"");
  return `"${normalized}"`;
}

function parseCsvRow(line: string) {
  return line.split(",").map((cell) => cell.trim());
}

function formatNumberLikeApi(value: string) {
  const parsed = Number(value);
  if (!Number.isFinite(parsed)) {
    return value;
  }
  return parsed.toLocaleString("ru-RU", { maximumFractionDigits: 8 });
}

function formatTimestampLabel(value: string) {
  const timestamp = Date.parse(value);
  if (Number.isNaN(timestamp)) {
    return value;
  }
  return new Date(timestamp).toISOString().replace(".000Z", "Z");
}

function normalizeUiTimeframeToApi(timeframe: UiTimeframe) {
  return backendIntervalByTimeframe[timeframe];
}

function parseTimeframeMinutes(timeframe: UiTimeframe) {
  if (timeframe === "5M") {
    return 5;
  }
  if (timeframe === "1H") {
    return 60;
  }
  return 1440;
}

function alignUtcDateToTimeframe(date: Date, timeframe: UiTimeframe) {
  const aligned = new Date(date);
  aligned.setUTCSeconds(0, 0);

  if (timeframe === "5M") {
    aligned.setUTCMinutes(Math.floor(aligned.getUTCMinutes() / 5) * 5);
    return aligned;
  }

  if (timeframe === "1H") {
    aligned.setUTCMinutes(0, 0, 0);
    return aligned;
  }

  aligned.setUTCHours(0, 0, 0, 0);
  return aligned;
}

function buildLatestRange(timeframe: UiTimeframe, count: number) {
  const to = alignUtcDateToTimeframe(new Date(), timeframe);
  const minutes = parseTimeframeMinutes(timeframe);
  const from = new Date(to.getTime() - (count - 1) * minutes * 60_000);

  return {
    from: from.toISOString(),
    to: to.toISOString(),
  };
}

function buildRangeFromDateInputs(from: string, to: string) {
  const fromDate = new Date(`${from}T00:00:00.000Z`);
  const toDateExclusive = new Date(`${to}T00:00:00.000Z`);
  toDateExclusive.setUTCDate(toDateExclusive.getUTCDate() + 1);

  return {
    from: fromDate.toISOString(),
    to: toDateExclusive.toISOString(),
  };
}

function formatApiErrorMessage(error: unknown) {
  if (error instanceof Error) {
    return error.message;
  }
  return "Не удалось выполнить запрос к backend.";
}

async function readJsonOrThrow<T>(response: Response): Promise<T> {
  const payload = await response.json().catch(() => null);

  if (!response.ok) {
    const message =
      typeof payload === "object" &&
      payload !== null &&
      "message" in payload &&
      typeof payload.message === "string"
        ? payload.message
        : `HTTP ${response.status}`;
    throw new Error(message);
  }

  return payload as T;
}

function mapCandleToRow(candle: CandleApiResponse): CandleTableRow {
  return {
    ts: formatTimestampLabel(candle.openTime),
    symbol: candle.symbol,
    open: formatNumberLikeApi(candle.open),
    high: formatNumberLikeApi(candle.high),
    low: formatNumberLikeApi(candle.low),
    close: formatNumberLikeApi(candle.close),
    volume: formatNumberLikeApi(candle.volume),
  };
}

function buildProfileFromCandles(
  candles: CandleApiResponse[],
  timeframe: UiTimeframe
): DatasetProfile {
  const sorted = [...candles].sort(
    (left, right) => Date.parse(left.openTime) - Date.parse(right.openTime)
  );
  const first = sorted[0];
  const last = sorted[sorted.length - 1];
  const symbols = new Set(sorted.map((candle) => candle.symbol));

  return {
    rowCount: `${sorted.length.toLocaleString("ru-RU")} строк`,
    dateRange:
      first && last
        ? `${formatTimestampLabel(first.openTime)} -> ${formatTimestampLabel(last.openTime)}`
        : "N/A",
    timeStep: timeframe,
    symbolCoverage: `${symbols.size} символов`,
  };
}

function sanitizeDatasetForPersistence(dataset: UiDataset): PersistedDatasetPayload {
  const { candleRows: _candleRows, mergedCsvUrl: _mergedCsvUrl, ...persistedDataset } = dataset;
  return persistedDataset;
}

function hydrateDataset(dataset: PersistedDatasetPayload): UiDataset {
  const raw = dataset as PersistedDatasetPayload & Record<string, unknown>;
  const sourceValue = typeof raw.source === "string" ? raw.source : "local";
  const symbolValue = typeof raw.symbol === "string" ? raw.symbol : null;
  const timeframeValue =
    typeof raw.timeframe === "string"
      ? raw.timeframe
      : typeof raw.interval === "string"
        ? raw.interval
        : "N/A";
  const rowsCount = typeof raw.rowsCount === "number" ? raw.rowsCount : null;
  const startAt = typeof raw.startAt === "string" ? raw.startAt : null;
  const endAt = typeof raw.endAt === "string" ? raw.endAt : null;
  const versionValue = typeof raw.version === "string" ? raw.version : dataset.datasetVersion;
  const normalizedSource =
    sourceValue === "bybit" &&
    dataset.backendRequest?.exchange === "binance"
      ? "binance"
      : sourceValue;
  const normalizedMarketType =
    normalizedSource === "moex" && dataset.marketType === "spot"
      ? "shares"
      : dataset.marketType ?? "spot";

  return {
    ...dataset,
    id: dataset.id,
    name: dataset.name ?? String(dataset.id),
    period: dataset.period ?? (startAt && endAt ? `${startAt} -> ${endAt}` : "N/A"),
    timeframe: dataset.timeframe ?? timeframeValue,
    symbols: dataset.symbols ?? (symbolValue ? [symbolValue] : ["N/A"]),
    size: dataset.size ?? (rowsCount === null ? "N/A" : `${rowsCount.toLocaleString("ru-RU")} свечей`),
    pipelineHash: dataset.pipelineHash ?? "canonical_candles",
    source: normalizedSource as DatasetSource,
    marketType: normalizedMarketType,
    archived: dataset.archived ?? false,
    loadStatus: dataset.loadStatus ?? "ready",
    profile: dataset.profile ?? {
      rowCount: rowsCount === null ? "N/A" : `${rowsCount.toLocaleString("ru-RU")} строк`,
      dateRange: startAt && endAt ? `${startAt} -> ${endAt}` : "N/A",
      timeStep: timeframeValue,
      symbolCoverage: symbolValue ? "1 символ" : "N/A",
    },
    compatibility: dataset.compatibility ?? {
      compatible: true,
      missingFields: [],
      notes: ["Canonical candles готовы для execution"],
    },
    tags: dataset.tags ?? buildDatasetTags(
      normalizedSource as DatasetSource,
      normalizedMarketType,
      timeframeValue,
      symbolValue ? [symbolValue] : ["N/A"]
    ),
    datasetVersion: versionValue,
    qualityStatus:
      dataset.qualityStatus ??
      (typeof raw.qualityStatus === "string" &&
      ["OK", "WARNING", "FAILED"].includes(raw.qualityStatus)
        ? (raw.qualityStatus as "OK" | "WARNING" | "FAILED")
        : undefined),
    qualityIssueCount:
      dataset.qualityIssueCount ??
      (Array.isArray(raw.qualityFlags) ? raw.qualityFlags.length : undefined),
  };
}

function normalizeColumnName(name: string) {
  return name.trim().toLowerCase().replace(/\s+/g, "_");
}

function formatTimeStep(minutes: number | null) {
  if (minutes === null || Number.isNaN(minutes) || minutes <= 0) {
    return "N/A";
  }
  if (minutes % 1440 === 0) {
    return `${minutes / 1440}D`;
  }
  if (minutes % 60 === 0) {
    return `${minutes / 60}H`;
  }
  return `${minutes}M`;
}

function formatCoverage(days: number) {
  const normalizedDays = Number.isInteger(days) ? String(days) : days.toFixed(2);
  const months = (days / 30.44).toFixed(1);
  return `${normalizedDays} дн. (${months} мес.)`;
}

function parseRangeCoverageDays(range: string) {
  if (!range.includes("->")) {
    return null;
  }

  const [rawStart, rawEnd] = range.split("->").map((value) => value.trim());
  const start = Date.parse(rawStart);
  const end = Date.parse(rawEnd);
  if (Number.isNaN(start) || Number.isNaN(end)) {
    return null;
  }

  const diffDays = Math.floor(Math.abs(end - start) / (24 * 60 * 60 * 1000)) + 1;
  return Math.max(1, diffDays);
}

function parseRowCount(rowCount: string) {
  const match = rowCount.match(/\d+/);
  return match ? Number(match[0]) : null;
}

function timeframeToMinutes(timeframe: string) {
  const match = timeframe.trim().match(/^(\d+)\s*([mhd])$/i);
  if (!match) {
    return null;
  }

  const value = Number(match[1]);
  const unit = match[2].toUpperCase();
  if (unit === "D") {
    return value * 1440;
  }
  if (unit === "H") {
    return value * 60;
  }
  return value;
}

function getDatasetCoverageLabel(dataset: UiDataset) {
  const byPeriod = parseRangeCoverageDays(dataset.period);
  if (byPeriod !== null) {
    return formatCoverage(byPeriod);
  }

  const byProfileRange = parseRangeCoverageDays(dataset.profile.dateRange);
  if (byProfileRange !== null) {
    return formatCoverage(byProfileRange);
  }

  const rows = parseRowCount(dataset.profile.rowCount);
  const minutesPerStep = timeframeToMinutes(dataset.timeframe);
  if (!rows || !minutesPerStep) {
    return "N/A";
  }

  const days = Number(((rows * minutesPerStep) / 1440).toFixed(2));
  if (days <= 0) {
    return "N/A";
  }

  return formatCoverage(days);
}

function buildDatasetTags(
  source: DatasetSource,
  market: MarketType,
  timeframe: string,
  symbols: string[]
) {
  return Array.from(
    new Set([
      source,
      market,
      timeframe.toLowerCase(),
      ...symbols.map((symbol) => symbol.toLowerCase()),
    ])
  );
}

function buildBacktestCompatibility(params: {
  source: DatasetSource;
  validation: CsvValidation | null;
}): BacktestCompatibility {
  if (params.source !== "local") {
    return {
      compatible: true,
      missingFields: [],
      notes: [`Формат ожидается от API-коннектора ${sourceLabels[params.source]}`],
    };
  }

  const missingFields = params.validation?.missingColumns ?? [];
  return {
    compatible: missingFields.length === 0,
    missingFields,
    notes:
      missingFields.length === 0
        ? ["CSV содержит обязательные поля для бэктеста"]
        : ["Нужно дополнить CSV обязательными колонками"],
  };
}

function getMarketTypeOptions(source: DatasetSource): MarketType[] {
  if (source === "moex") {
    return ["shares", "futures"];
  }
  if (source === "local") {
    return ["spot", "futures", "shares"];
  }
  return ["spot", "futures"];
}

function getSourceRequestOptions(source: ExchangeSource, marketType: MarketType) {
  if (source === "binance") {
    return {};
  }
  if (source === "bybit") {
    return {
      category: marketType === "futures" ? "linear" : "spot",
    };
  }

  return marketType === "futures"
    ? {
        engine: "futures",
        market: "forts",
      }
    : {
        engine: "stock",
        market: "shares",
        board: "TQBR",
      };
}

async function validateCsvFiles(files: File[]): Promise<CsvValidationResult> {
  if (files.length === 0) {
    return { validation: null, issues: [] };
  }

  let headerColumns: string[] = [];
  const timestampSeen = new Set<string>();
  const issues: CsvImportIssue[] = [];

  let duplicateTimestampCount = 0;
  let missingValueCount = 0;
  let unsortedCount = 0;
  let totalRows = 0;
  const distinctSymbols = new Set<string>();
  const stepSamples: number[] = [];
  let minTimestampValue: number | null = null;
  let maxTimestampValue: number | null = null;

  for (const file of files) {
    const text = await file.text();
    const lines = text
      .split(/\r?\n/)
      .map((line) => line.trim())
      .filter((line) => line.length > 0);

    if (lines.length === 0) {
      continue;
    }

    const rawHeader = parseCsvRow(lines[0]);
    const normalizedHeader = rawHeader.map(normalizeColumnName);

    if (headerColumns.length === 0) {
      headerColumns = normalizedHeader;
    }

    const missingColumnsInFile = requiredCsvColumns.filter(
      (requiredColumn) => !normalizedHeader.includes(requiredColumn)
    );
    missingColumnsInFile.forEach((column) => {
      issues.push({
        fileName: file.name,
        rowNumber: 1,
        timestamp: "-",
        column,
        issue: "missing_column",
        value: "",
      });
    });

    const timestampIndex = normalizedHeader.indexOf("timestamp");
    const symbolIndex = normalizedHeader.indexOf("symbol");

    let previousTimestamp: number | null = null;

    for (let rowIndex = 1; rowIndex < lines.length; rowIndex += 1) {
      const cells = parseCsvRow(lines[rowIndex]);
      totalRows += 1;

      for (const requiredColumn of requiredCsvColumns) {
        const columnIndex = normalizedHeader.indexOf(requiredColumn);
        if (columnIndex === -1) {
          continue;
        }

        const value = cells[columnIndex];
        if (value === undefined || value === "") {
          missingValueCount += 1;
          issues.push({
            fileName: file.name,
            rowNumber: rowIndex + 1,
            timestamp: timestampIndex !== -1 ? cells[timestampIndex] ?? "-" : "-",
            column: requiredColumn,
            issue: "missing_value",
            value: "",
          });
        }
      }

      if (timestampIndex !== -1) {
        const timestampValue = cells[timestampIndex];
        if (timestampValue) {
          if (timestampSeen.has(timestampValue)) {
            duplicateTimestampCount += 1;
            issues.push({
              fileName: file.name,
              rowNumber: rowIndex + 1,
              timestamp: timestampValue,
              column: "timestamp",
              issue: "duplicate_timestamp",
              value: timestampValue,
            });
          } else {
            timestampSeen.add(timestampValue);
          }

          const asEpoch = Number(new Date(timestampValue));
          if (!Number.isNaN(asEpoch)) {
            if (minTimestampValue === null || asEpoch < minTimestampValue) {
              minTimestampValue = asEpoch;
            }
            if (maxTimestampValue === null || asEpoch > maxTimestampValue) {
              maxTimestampValue = asEpoch;
            }
            if (previousTimestamp !== null && asEpoch < previousTimestamp) {
              unsortedCount += 1;
              issues.push({
                fileName: file.name,
                rowNumber: rowIndex + 1,
                timestamp: timestampValue,
                column: "timestamp",
                issue: "unsorted_timestamp",
                value: timestampValue,
              });
            }
            if (previousTimestamp !== null && asEpoch > previousTimestamp) {
              stepSamples.push((asEpoch - previousTimestamp) / 60000);
            }
            previousTimestamp = asEpoch;
          }
        }
      }

      if (symbolIndex !== -1) {
        const symbolValue = cells[symbolIndex];
        if (symbolValue) {
          distinctSymbols.add(symbolValue);
        }
      }
    }
  }

  const missingColumns = requiredCsvColumns.filter(
    (requiredColumn) => !headerColumns.includes(requiredColumn)
  );

  const averageStep =
    stepSamples.length > 0
      ? stepSamples.reduce((sum, value) => sum + value, 0) / stepSamples.length
      : null;

  return {
    validation: {
      isValid:
        missingColumns.length === 0 &&
        duplicateTimestampCount === 0 &&
        missingValueCount === 0 &&
        unsortedCount === 0,
      detectedColumns: headerColumns,
      missingColumns,
      duplicateTimestampCount,
      missingValueCount,
      unsortedCount,
      totalRows,
      dateRangeStart: minTimestampValue
        ? new Date(minTimestampValue).toISOString().slice(0, 10)
        : null,
      dateRangeEnd: maxTimestampValue
        ? new Date(maxTimestampValue).toISOString().slice(0, 10)
        : null,
      inferredStep: formatTimeStep(averageStep ? Math.round(averageStep) : null),
      symbolCoverage:
        distinctSymbols.size > 0
          ? `${distinctSymbols.size} символов`
          : "N/A",
    },
    issues,
  };
}

export default function DataPage() {
  const [datasets, setDatasets] = useState<UiDataset[]>([]);
  const [selectedDatasetId, setSelectedDatasetId] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [filterSource, setFilterSource] = useState<"all" | DatasetSource>("all");
  const [filterMarket, setFilterMarket] = useState<"all" | MarketType>("all");
  const [filterTimeframe, setFilterTimeframe] = useState("all");
  const [filterSymbol, setFilterSymbol] = useState("all");
  const [showArchived, setShowArchived] = useState(false);
  const [renameDraft, setRenameDraft] = useState("");

  const [createOpen, setCreateOpen] = useState(false);
  const [datasetName, setDatasetName] = useState("");
  const [datasetSource, setDatasetSource] = useState<DatasetSource>("binance");
  const [marketType, setMarketType] = useState<MarketType>("spot");
  const [symbolsInput, setSymbolsInput] = useState("BTCUSDT");
  const [timeframe, setTimeframe] = useState<UiTimeframe>("1H");
  const [binanceLoadMode, setBinanceLoadMode] = useState<BinanceLoadMode>("latest");
  const [recentCandles, setRecentCandles] = useState("500");
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [uploadedCsvFiles, setUploadedCsvFiles] = useState<File[]>([]);
  const [csvValidation, setCsvValidation] = useState<CsvValidation | null>(null);
  const [csvImportIssues, setCsvImportIssues] = useState<CsvImportIssue[]>([]);
  const [localCsvMode, setLocalCsvMode] = useState<LocalCsvMode | null>(null);
  const [mergeError, setMergeError] = useState<string | null>(null);
  const [mergedCsv, setMergedCsv] = useState<MergedCsv | null>(null);
  const [isImportingCandles, setIsImportingCandles] = useState(false);
  const [isLoadingDatasets, setIsLoadingDatasets] = useState(true);
  const [importTemplates, setImportTemplates] =
    useState<ImportTemplate[]>(defaultImportTemplates);
  const [selectedTemplateId, setSelectedTemplateId] = useState("none");
  const [templateNameDraft, setTemplateNameDraft] = useState("");
  const [requestedDataset, setRequestedDataset] = useState("");

  const selectedDataset = useMemo(
    () => datasets.find((dataset) => dataset.id === selectedDatasetId) ?? null,
    [datasets, selectedDatasetId]
  );
  const selectedDatasetRows = useMemo<CandleTableRow[]>(
    () =>
      selectedDataset?.candleRows?.length
        ? selectedDataset.candleRows
        : previewRows.map((row) => ({
            ts: row.ts,
            symbol: selectedDataset?.symbols[0] ?? "N/A",
            open: String(row.open),
            high: String(row.high),
            low: String(row.low),
            close: String(row.close),
            volume: String(row.volume),
          })),
    [selectedDataset]
  );

  useEffect(() => {
    const allowedTimeframes = datasetSource === "local" ? exchangeTimeframes.binance : exchangeTimeframes[datasetSource];
    if (!allowedTimeframes.includes(timeframe)) {
      setTimeframe(allowedTimeframes[0]);
    }

    const allowedMarkets = getMarketTypeOptions(datasetSource);
    if (!allowedMarkets.includes(marketType)) {
      setMarketType(allowedMarkets[0]);
    }
  }, [datasetSource, timeframe, marketType]);

  useEffect(() => {
    let cancelled = false;

    async function loadDatasets() {
      setIsLoadingDatasets(true);

      try {
        const response = await apiFetch("/api/datasets", { cache: "no-store" });
        const payload = await readJsonOrThrow<PersistedDatasetPayload[]>(response);

        if (!cancelled) {
          setDatasets(payload.map(hydrateDataset));
        }
      } catch (error) {
        if (!cancelled) {
          setMergeError(formatApiErrorMessage(error));
        }
      } finally {
        if (!cancelled) {
          setIsLoadingDatasets(false);
        }
      }
    }

    loadDatasets();

    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (!selectedDataset?.backendRequest || selectedDataset.candleRows?.length) {
      return;
    }

    const datasetToLoad = selectedDataset;

    let cancelled = false;

    async function loadDatasetCandles() {
      try {
        const collectedCandles: CandleApiResponse[] = [];

        for (const symbol of datasetToLoad.symbols) {
          const query = new URLSearchParams({
            exchange: datasetToLoad.backendRequest?.exchange ?? "binance",
            symbol,
            interval: datasetToLoad.backendRequest?.interval ?? "1h",
            from: datasetToLoad.backendRequest?.from ?? "",
            to: datasetToLoad.backendRequest?.to ?? "",
          });
          const response = await apiFetch(`/api/candles?${query.toString()}`, {
            cache: "no-store",
          });
          const payload = await readJsonOrThrow<CandleApiResponse[]>(response);
          collectedCandles.push(...payload);
        }

        if (!cancelled) {
          const candleRows = collectedCandles
            .sort((left, right) => Date.parse(left.openTime) - Date.parse(right.openTime))
            .map(mapCandleToRow);

          setDatasets((prev) =>
            prev.map((dataset) =>
              dataset.id === datasetToLoad.id ? { ...dataset, candleRows } : dataset
            )
          );
        }
      } catch (error) {
        if (!cancelled) {
          setMergeError(formatApiErrorMessage(error));
        }
      }
    }

    loadDatasetCandles();

    return () => {
      cancelled = true;
    };
  }, [selectedDataset]);

  useEffect(() => {
    setRenameDraft(selectedDataset?.name ?? "");
  }, [selectedDataset?.id, selectedDataset?.name]);

  useEffect(() => {
    if (!selectedDataset?.id) {
      return;
    }

    let cancelled = false;
    const selectedId = selectedDataset.id;

    async function loadDatasetPlatformMetadata() {
      try {
        const [versionsResponse, qualityResponse] = await Promise.all([
          apiFetch(`/api/datasets/${selectedId}/versions`, { cache: "no-store" }),
          apiFetch(`/api/datasets/${selectedId}/quality`, { cache: "no-store" }),
        ]);
        const versions = await readJsonOrThrow<DatasetSnapshotApiResponse[]>(versionsResponse);
        const quality = await readJsonOrThrow<DatasetQualityApiResponse[]>(qualityResponse);
        const latestVersion = versions[0];
        const latestQuality = quality[0];

        if (!cancelled) {
          setDatasets((prev) =>
            prev.map((dataset) =>
              dataset.id === selectedId
                ? {
                    ...dataset,
                    datasetVersion: latestVersion?.datasetVersion ?? dataset.datasetVersion,
                    latestSnapshotId: latestVersion?.id ?? dataset.latestSnapshotId,
                    qualityStatus: latestQuality?.qualityStatus ?? dataset.qualityStatus,
                    qualityIssueCount: latestQuality?.issues?.length ?? dataset.qualityIssueCount,
                  }
                : dataset
            )
          );
        }
      } catch {
        // The dataset list remains usable even when platform metadata is unavailable.
      }
    }

    void loadDatasetPlatformMetadata();

    return () => {
      cancelled = true;
    };
  }, [selectedDataset?.id]);

  useEffect(() => {
    if (typeof window === "undefined") {
      return;
    }

    const datasetFromUrl =
      new URLSearchParams(window.location.search).get("dataset")?.trim() ?? "";
    setRequestedDataset(datasetFromUrl);
  }, []);

  useEffect(() => {
    if (!requestedDataset || datasets.length === 0) {
      return;
    }

    const normalizedRequestedDataset = requestedDataset.toLowerCase();
    const matchingDataset =
      datasets.find(
        (dataset) => dataset.id === requestedDataset || dataset.name === requestedDataset
      ) ??
      datasets.find(
        (dataset) =>
          dataset.id.toLowerCase() === normalizedRequestedDataset ||
          dataset.name.toLowerCase() === normalizedRequestedDataset
      ) ??
      datasets.find((dataset) =>
        dataset.name.toLowerCase().includes(normalizedRequestedDataset)
      );

    if (!matchingDataset) {
      if (searchQuery !== requestedDataset) {
        setSearchQuery(requestedDataset);
      }
      return;
    }

    if (matchingDataset.archived && !showArchived) {
      setShowArchived(true);
    }

    if (selectedDatasetId !== matchingDataset.id) {
      setSelectedDatasetId(matchingDataset.id);
    }
  }, [datasets, requestedDataset, searchQuery, selectedDatasetId, showArchived]);

  const availableTimeframes = useMemo(
    () =>
      Array.from(new Set(datasets.map((dataset) => dataset.timeframe))).sort(),
    [datasets]
  );

  const availableSymbols = useMemo(
    () =>
      Array.from(
        new Set(
          datasets.flatMap((dataset) =>
            dataset.symbols.map((symbol) => symbol.toUpperCase())
          )
        )
      ).sort(),
    [datasets]
  );

  const filteredDatasets = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();

    return datasets.filter((dataset) => {
      const archivedMatch = showArchived ? true : !dataset.archived;
      const sourceMatch = filterSource === "all" || dataset.source === filterSource;
      const marketMatch = filterMarket === "all" || dataset.marketType === filterMarket;
      const timeframeMatch =
        filterTimeframe === "all" || dataset.timeframe === filterTimeframe;
      const symbolMatch =
        filterSymbol === "all" ||
        dataset.symbols.some((symbol) => symbol.toUpperCase() === filterSymbol);
      const searchMatch =
        q.length === 0 ||
        dataset.name.toLowerCase().includes(q) ||
        dataset.tags.some((tag) => tag.includes(q));

      return (
        archivedMatch &&
        sourceMatch &&
        marketMatch &&
        timeframeMatch &&
        symbolMatch &&
        searchMatch &&
        dataset.loadStatus !== "error"
      );
    });
  }, [datasets, filterMarket, filterSource, filterSymbol, filterTimeframe, searchQuery, showArchived]);

  useEffect(() => {
    if (filteredDatasets.length === 0) {
      setSelectedDatasetId(null);
      return;
    }

    if (!selectedDatasetId) {
      return;
    }

    if (!filteredDatasets.some((dataset) => dataset.id === selectedDatasetId)) {
      setSelectedDatasetId(null);
    }
  }, [filteredDatasets, selectedDatasetId]);

  useEffect(() => {
    const queuedIds = datasets
      .filter((dataset) => dataset.loadStatus === "queued")
      .map((dataset) => dataset.id);

    if (queuedIds.length === 0) {
      return;
    }

    const toProcessingTimer = setTimeout(() => {
      setDatasets((prev) =>
        prev.map((dataset) =>
          queuedIds.includes(dataset.id)
            ? { ...dataset, loadStatus: "processing", rowsHint: "Идёт загрузка" }
            : dataset
        )
      );
    }, 1200);

    const toReadyTimer = setTimeout(() => {
      setDatasets((prev) =>
        prev.map((dataset) =>
          queuedIds.includes(dataset.id)
            ? { ...dataset, loadStatus: "ready", rowsHint: "Импорт завершён" }
            : dataset
        )
      );
    }, 2800);

    return () => {
      clearTimeout(toProcessingTimer);
      clearTimeout(toReadyTimer);
    };
  }, [datasets]);

  function resetCreateForm(options?: { preserveMergedCsv?: boolean }) {
    if (!options?.preserveMergedCsv && mergedCsv?.url) {
      URL.revokeObjectURL(mergedCsv.url);
    }

    setDatasetName("");
    setDatasetSource("binance");
    setMarketType("spot");
    setSymbolsInput("BTCUSDT");
    setTimeframe("1H");
    setBinanceLoadMode("latest");
    setRecentCandles("500");
    setDateFrom("");
    setDateTo("");
    setUploadedCsvFiles([]);
    setCsvValidation(null);
    setCsvImportIssues([]);
    setLocalCsvMode(null);
    setMergeError(null);
    setMergedCsv(null);
    setSelectedTemplateId("none");
    setTemplateNameDraft("");
  }

  function applyImportTemplate(template: ImportTemplate) {
    setDatasetSource(template.source);
    setMarketType(template.marketType);
    setSymbolsInput(template.symbolsInput);
    setTimeframe(template.timeframe);
    setBinanceLoadMode(template.binanceLoadMode);
    setRecentCandles(template.recentCandles);
    setDateFrom(template.dateFrom);
    setDateTo(template.dateTo);
    setLocalCsvMode(template.source === "local" ? template.localCsvMode : null);
    setMergeError(null);

    if (template.source !== "local") {
      if (mergedCsv?.url) {
        URL.revokeObjectURL(mergedCsv.url);
      }
      setUploadedCsvFiles([]);
      setCsvValidation(null);
      setCsvImportIssues([]);
      setMergedCsv(null);
    }
  }

  function handleTemplateSelect(templateId: string) {
    setSelectedTemplateId(templateId);
    if (templateId === "none") {
      return;
    }
    const template = importTemplates.find((item) => item.id === templateId);
    if (!template) {
      return;
    }
    applyImportTemplate(template);
  }

  function handleSaveImportTemplate() {
    const normalizedName = templateNameDraft.trim();
    const template: ImportTemplate = {
      id: `tpl-custom-${Date.now()}`,
      name:
        normalizedName.length > 0
          ? normalizedName
          : `${datasetSource === "local" ? "CSV" : sourceLabels[datasetSource]} preset ${importTemplates.length + 1}`,
      source: datasetSource,
      marketType,
      symbolsInput,
      timeframe,
      binanceLoadMode,
      recentCandles,
      dateFrom,
      dateTo,
      localCsvMode: localCsvMode ?? "separate",
    };
    setImportTemplates((prev) => [template, ...prev]);
    setSelectedTemplateId(template.id);
    setTemplateNameDraft("");
  }

  function handleDeleteImportTemplate() {
    if (selectedTemplateId === "none") {
      return;
    }
    setImportTemplates((prev) =>
      prev.filter((template) => template.id !== selectedTemplateId)
    );
    setSelectedTemplateId("none");
  }

  async function handleCsvFileChange(event: ChangeEvent<HTMLInputElement>) {
    const files = event.target.files ? Array.from(event.target.files) : [];

    if (mergedCsv?.url) {
      URL.revokeObjectURL(mergedCsv.url);
      setMergedCsv(null);
    }

    setUploadedCsvFiles(files);
    setLocalCsvMode(files.length > 1 ? null : "separate");
    setMergeError(null);

    const validationResult = await validateCsvFiles(files);
    setCsvValidation(validationResult.validation);
    setCsvImportIssues(validationResult.issues);
  }

  async function handleMergeCsv() {
    if (uploadedCsvFiles.length < 2) {
      setMergeError("Для склейки фьючерсных контрактов выберите минимум 2 CSV файла.");
      return;
    }

    try {
      let header: string | null = null;
      const mergedDataRows: string[] = [];

      for (const file of uploadedCsvFiles) {
        const raw = await file.text();
        const lines = raw
          .split(/\r?\n/)
          .map((line) => line.trim())
          .filter((line) => line.length > 0);

        if (lines.length === 0) {
          continue;
        }

        const [fileHeader, ...fileRows] = lines;

        if (!header) {
          header = fileHeader;
          mergedDataRows.push(...fileRows);
          continue;
        }

        if (fileHeader === header) {
          mergedDataRows.push(...fileRows);
        } else {
          mergedDataRows.push(fileHeader, ...fileRows);
        }
      }

      if (!header) {
        setMergeError("Файлы пустые: не удалось сформировать общий CSV.");
        return;
      }

      const mergedPayload = [header, ...mergedDataRows].join("\n");
      const blob = new Blob([mergedPayload], { type: "text/csv;charset=utf-8" });
      const url = URL.createObjectURL(blob);

      if (mergedCsv?.url) {
        URL.revokeObjectURL(mergedCsv.url);
      }

      setMergedCsv({
        url,
        name: `futures-merged-${Date.now()}.csv`,
        rows: mergedDataRows.length,
        size: blob.size,
      });
      setMergeError(null);
    } catch {
      setMergeError("Ошибка чтения CSV файлов. Проверьте формат и попробуйте снова.");
    }
  }

  function handleExportImportIssues() {
    if (csvImportIssues.length === 0) {
      return;
    }

    const header = ["file", "row", "timestamp", "column", "issue", "value"];
    const rows = csvImportIssues.map((issue) => [
      issue.fileName,
      String(issue.rowNumber),
      issue.timestamp,
      issue.column,
      importIssueLabels[issue.issue],
      issue.value,
    ]);

    const payload = [header, ...rows]
      .map((row) => row.map((cell) => escapeCsvCell(cell)).join(","))
      .join("\n");

    const blob = new Blob([payload], { type: "text/csv;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `import-issues-${Date.now()}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  }

  async function handleAddDataset() {
    const normalizedRecentCandles = Number.parseInt(recentCandles.trim(), 10);
    const hasValidRecentCandles =
      Number.isFinite(normalizedRecentCandles) && normalizedRecentCandles > 0;

    if (
      datasetSource === "local" &&
      csvValidation &&
      !csvValidation.isValid
    ) {
      setMergeError("Исправьте ошибки CSV перед добавлением датасета.");
      return;
    }

    if (
      datasetSource === "local" &&
      uploadedCsvFiles.length > 1 &&
      localCsvMode === null
    ) {
      setMergeError("Выберите режим обработки: смёрджить CSV или оставить по отдельности.");
      return;
    }

    if (
      datasetSource === "local" &&
      uploadedCsvFiles.length > 1 &&
      localCsvMode === "merge" &&
      !mergedCsv
    ) {
      setMergeError("Нажмите «Склеить CSV», чтобы сформировать объединенный файл.");
      return;
    }

    if (datasetSource !== "local" && binanceLoadMode === "latest" && !hasValidRecentCandles) {
      setMergeError("Укажите корректное количество последних свечей (N > 0).");
      return;
    }

    if (datasetSource !== "local" && binanceLoadMode === "range" && (!dateFrom || !dateTo)) {
      setMergeError("Для режима диапазона заполните даты from и to.");
      return;
    }

    if (datasetSource !== "local" && binanceLoadMode === "range" && dateFrom > dateTo) {
      setMergeError("Дата from не может быть позже даты to.");
      return;
    }

    const symbols = symbolsInput
      .split(/[\s,]+/)
      .map((symbol) => symbol.trim())
      .filter(Boolean);

    if (datasetSource !== "local" && symbols.length === 0) {
      setMergeError("Укажите хотя бы один символ.");
      return;
    }

    const uploadedSize = uploadedCsvFiles.reduce((total, file) => total + file.size, 0);

    const datasetId = `custom-${Date.now()}`;
    const isLocal = datasetSource === "local";
    const isExchangeLatestMode = datasetSource !== "local" && binanceLoadMode === "latest";
    const isMergeMode = isLocal && localCsvMode === "merge";
    const apiInterval = normalizeUiTimeframeToApi(timeframe);

    if (!isLocal) {
      const requestRange = isExchangeLatestMode
        ? buildLatestRange(timeframe, normalizedRecentCandles)
        : buildRangeFromDateInputs(dateFrom, dateTo);
      const sourceRequestOptions = getSourceRequestOptions(datasetSource, marketType);

      setMergeError(null);
      setIsImportingCandles(true);

      try {
        const collectedCandles: CandleApiResponse[] = [];
        let importedCount = 0;
        let latestDatasetMetadata: Record<string, unknown> | null = null;

        for (const symbol of symbols) {
          const importResponse = await apiFetch("/api/imports/candles", {
            method: "POST",
            headers: {
              "content-type": "application/json",
            },
            body: JSON.stringify({
              exchange: datasetSource,
              symbol,
              interval: apiInterval,
              from: requestRange.from,
              to: requestRange.to,
              ...sourceRequestOptions,
            }),
          });
          const importPayload = await readJsonOrThrow<ImportCandlesApiResponse>(importResponse);
          importedCount += importPayload.imported;
          latestDatasetMetadata = importPayload.dataset ?? latestDatasetMetadata;

          const query = new URLSearchParams({
            exchange: datasetSource,
            symbol,
            interval: apiInterval,
            from: requestRange.from,
            to: requestRange.to,
          });
          const candlesResponse = await apiFetch(`/api/candles?${query.toString()}`, {
            cache: "no-store",
          });
          const candlesPayload = await readJsonOrThrow<CandleApiResponse[]>(candlesResponse);
          collectedCandles.push(...candlesPayload);
        }

        const sortedCandles = [...collectedCandles].sort(
          (left, right) => Date.parse(left.openTime) - Date.parse(right.openTime)
        );
        const candleRows = sortedCandles.map(mapCandleToRow);
        const periodLabel = isExchangeLatestMode
          ? `Последние ${normalizedRecentCandles.toLocaleString("ru-RU")} свечей`
          : `${dateFrom} -> ${dateTo}`;
        const newDataset: UiDataset = {
          id: datasetId,
          name: datasetName.trim() || `Новый датасет ${datasets.length + 1}`,
          period: periodLabel,
          timeframe,
          symbols,
          size: `${sortedCandles.length.toLocaleString("ru-RU")} свечей`,
          pipelineHash: `api_${datasetSource}_candles`,
          source: datasetSource,
          marketType,
          loadStatus: "ready",
          archived: false,
          profile: buildProfileFromCandles(sortedCandles, timeframe),
          compatibility: buildBacktestCompatibility({
            source: datasetSource,
            validation: null,
          }),
          tags: buildDatasetTags(datasetSource, marketType, timeframe, symbols),
          rowsHint: `Импортировано ${importedCount.toLocaleString("ru-RU")} свечей`,
          candleRows,
          backendRequest: {
            exchange: datasetSource,
            interval: apiInterval,
            from: requestRange.from,
            to: requestRange.to,
          },
          datasetVersion:
            typeof latestDatasetMetadata?.version === "string"
              ? latestDatasetMetadata.version
              : undefined,
          qualityStatus:
            typeof latestDatasetMetadata?.qualityStatus === "string" &&
            ["OK", "WARNING", "FAILED"].includes(latestDatasetMetadata.qualityStatus)
              ? (latestDatasetMetadata.qualityStatus as "OK" | "WARNING" | "FAILED")
              : undefined,
          qualityIssueCount: Array.isArray(latestDatasetMetadata?.qualityFlags)
            ? latestDatasetMetadata.qualityFlags.length
            : undefined,
        };

        const persistedResponse = await apiFetch("/api/datasets", {
          method: "POST",
          headers: {
            "content-type": "application/json",
          },
          body: JSON.stringify(sanitizeDatasetForPersistence(newDataset)),
        });
        const persistedDataset = hydrateDataset(
          await readJsonOrThrow<PersistedDatasetPayload>(persistedResponse)
        );

        setDatasets((prev) => [{ ...persistedDataset, candleRows }, ...prev]);
        setSelectedDatasetId(datasetId);
        setCreateOpen(false);
        resetCreateForm({ preserveMergedCsv: Boolean(mergedCsv) });
        return;
      } catch (error) {
        setMergeError(formatApiErrorMessage(error));
        return;
      } finally {
        setIsImportingCandles(false);
      }
    }

    const periodLabel = isLocal
      ? "Локальный импорт"
      : isExchangeLatestMode
        ? `Последние ${normalizedRecentCandles.toLocaleString("ru-RU")} свечей`
        : `${dateFrom} -> ${dateTo}`;
    const compatibility = buildBacktestCompatibility({
      source: datasetSource,
      validation: csvValidation,
    });
    const profile: DatasetProfile =
      isLocal && csvValidation
        ? {
            rowCount: `${csvValidation.totalRows} строк`,
            dateRange:
              csvValidation.dateRangeStart && csvValidation.dateRangeEnd
                ? `${csvValidation.dateRangeStart} -> ${csvValidation.dateRangeEnd}`
                : "N/A",
            timeStep: csvValidation.inferredStep,
            symbolCoverage: csvValidation.symbolCoverage,
          }
        : {
            rowCount: "Ожидается",
            dateRange: periodLabel,
            timeStep: timeframe,
            symbolCoverage: `${symbols.length} символов`,
          };
    const newDataset: UiDataset = {
      id: datasetId,
      name: datasetName.trim() || `Новый датасет ${datasets.length + 1}`,
      period: periodLabel,
      timeframe: isLocal ? "N/A" : timeframe,
      symbols: isLocal ? ["CSV"] : symbols.length > 0 ? symbols : ["N/A"],
      size: isMergeMode && mergedCsv ? formatBytes(mergedCsv.size) : formatBytes(uploadedSize),
      pipelineHash: isMergeMode ? "csv_merge_local" : "dataset_pending",
      source: datasetSource,
      marketType: isLocal ? "spot" : marketType,
      loadStatus: "ready",
      archived: false,
      profile,
      compatibility,
      tags: buildDatasetTags(
        datasetSource,
        isLocal ? "spot" : marketType,
        isLocal ? "N/A" : timeframe,
        isLocal ? ["CSV"] : symbols.length > 0 ? symbols : ["N/A"]
      ),
      rowsHint: isMergeMode && mergedCsv
        ? `${mergedCsv.rows.toLocaleString("ru-RU")} строк (merged)`
        : uploadedCsvFiles.length > 1
          ? `${uploadedCsvFiles.length} файл(ов) по отдельности`
          : uploadedCsvFiles.length > 0
            ? "1 CSV файл"
            : "Черновик",
      mergedCsvName: isMergeMode ? mergedCsv?.name : undefined,
      mergedCsvRows: isMergeMode ? mergedCsv?.rows : undefined,
      mergedCsvUrl: isMergeMode ? mergedCsv?.url : undefined,
    };

    try {
      const persistedResponse = await apiFetch("/api/datasets", {
        method: "POST",
        headers: {
          "content-type": "application/json",
        },
        body: JSON.stringify(sanitizeDatasetForPersistence(newDataset)),
      });
      const persistedDataset = hydrateDataset(
        await readJsonOrThrow<PersistedDatasetPayload>(persistedResponse)
      );

      setDatasets((prev) => [
        {
          ...persistedDataset,
          mergedCsvUrl: newDataset.mergedCsvUrl,
        },
        ...prev,
      ]);
    } catch (error) {
      setMergeError(formatApiErrorMessage(error));
      return;
    }

    setSelectedDatasetId(datasetId);
    setCreateOpen(false);
    resetCreateForm({ preserveMergedCsv: Boolean(mergedCsv) });
  }

  async function handleRenameDataset() {
    if (!selectedDataset) {
      return;
    }
    const nextName = renameDraft.trim();
    if (!nextName) {
      return;
    }

    try {
      const response = await apiFetch(`/api/datasets/${selectedDataset.id}`, {
        method: "PATCH",
        headers: {
          "content-type": "application/json",
        },
        body: JSON.stringify({ name: nextName }),
      });
      const persistedDataset = hydrateDataset(
        await readJsonOrThrow<PersistedDatasetPayload>(response)
      );

      setDatasets((prev) =>
        prev.map((dataset) =>
          dataset.id === selectedDataset.id
            ? {
                ...persistedDataset,
                candleRows: dataset.candleRows,
                mergedCsvUrl: dataset.mergedCsvUrl,
              }
            : dataset
        )
      );
    } catch (error) {
      setMergeError(formatApiErrorMessage(error));
    }
  }

  async function handleDuplicateDataset() {
    if (!selectedDataset) {
      return;
    }

    try {
      const response = await apiFetch(`/api/datasets/${selectedDataset.id}/duplicate`, {
        method: "POST",
      });
      const persistedDataset = hydrateDataset(
        await readJsonOrThrow<PersistedDatasetPayload>(response)
      );
      const duplicatedDataset: UiDataset = {
        ...persistedDataset,
        candleRows: selectedDataset.candleRows,
      };

      setDatasets((prev) => [duplicatedDataset, ...prev]);
      setSelectedDatasetId(duplicatedDataset.id);
    } catch (error) {
      setMergeError(formatApiErrorMessage(error));
    }
  }

  async function handleDeleteDataset() {
    if (!selectedDataset) {
      return;
    }

    try {
      const response = await apiFetch(`/api/datasets/${selectedDataset.id}`, {
        method: "DELETE",
      });
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      setDatasets((prev) => prev.filter((dataset) => dataset.id !== selectedDataset.id));
      setSelectedDatasetId(null);
    } catch (error) {
      setMergeError(formatApiErrorMessage(error));
    }
  }

  const isExchangeForm = datasetSource !== "local";
  const shouldAskLocalCsvMode =
    datasetSource === "local" && uploadedCsvFiles.length > 1;
  const showMergePanel = datasetSource === "local" && localCsvMode === "merge";

  return (
    <div className="flex min-h-full flex-col gap-5">
      <PageHeader
        title="Данные"
      />

      <div className="overflow-x-auto pb-1 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden">
        <div className="grid min-w-[980px] grid-cols-5 gap-3">
          {dataSources.map((source) => (
            <SurfaceCard
              key={source.id}
              className="py-0"
              contentClassName="relative flex min-h-[88px] items-center overflow-hidden px-5 py-4"
            >
              <div className="relative z-10 min-w-0 pr-12">
                <div className="truncate text-lg font-semibold tracking-tight text-foreground">
                  {source.name}
                </div>
              </div>
              <div className="pointer-events-none absolute inset-y-0 right-0 flex aspect-square h-full translate-x-1/2 items-center justify-center">
                {source.iconSrc ? (
                  <Image
                    src={source.iconSrc}
                    alt=""
                    width={96}
                    height={96}
                    className="h-full w-full object-contain opacity-90"
                    aria-hidden="true"
                  />
                ) : (
                  <Database className="h-full w-full text-muted-foreground/90" />
                )}
              </div>
            </SurfaceCard>
          ))}
        </div>
      </div>

      <SurfaceCard
        title="Теги и поиск"
        subtitle="Фильтруйте датасеты по источнику, рынку, таймфрейму и символу."
        className="border-border/80 bg-[linear-gradient(140deg,hsl(var(--tl-bg-1)/0.98),hsl(var(--tl-bg-2)/0.94))]"
        actions={
          <Button
            size="sm"
            variant="secondary"
            onClick={() => setShowArchived((value) => !value)}
          >
            {showArchived ? "Скрыть архив" : "Показать архив"}
          </Button>
        }
      >
        <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-5">
          <div className="xl:col-span-2">
            <div className="mb-1 text-xs text-muted-foreground">Поиск</div>
            <Input
              value={searchQuery}
              onChange={(event) => setSearchQuery(event.target.value)}
              placeholder="Название, тег, символ"
            />
          </div>
          <div>
            <div className="mb-1 text-xs text-muted-foreground">Источник</div>
            <Select
              value={filterSource}
              onValueChange={(value) => setFilterSource(value as "all" | DatasetSource)}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">Все</SelectItem>
                  <SelectItem value="binance">Binance</SelectItem>
                  <SelectItem value="bybit">Bybit</SelectItem>
                  <SelectItem value="moex">MOEX</SelectItem>
                  <SelectItem value="local">Локально</SelectItem>
                </SelectContent>
              </Select>
          </div>
          <div>
            <div className="mb-1 text-xs text-muted-foreground">Рынок</div>
            <Select
              value={filterMarket}
              onValueChange={(value) => setFilterMarket(value as "all" | MarketType)}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Все</SelectItem>
                <SelectItem value="spot">Spot</SelectItem>
                <SelectItem value="futures">Futures</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div>
            <div className="mb-1 text-xs text-muted-foreground">ТФ / Символ</div>
            <div className="grid grid-cols-2 gap-2">
              <Select value={filterTimeframe} onValueChange={setFilterTimeframe}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">ТФ: все</SelectItem>
                  {availableTimeframes.map((item) => (
                    <SelectItem key={item} value={item}>
                      {item}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Select value={filterSymbol} onValueChange={setFilterSymbol}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">Символ: все</SelectItem>
                  {availableSymbols.map((item) => (
                    <SelectItem key={item} value={item}>
                      {item}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
        </div>
      </SurfaceCard>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-[360px_minmax(0,1fr)]">
        <SurfaceCard
          title="Список датасетов"
          subtitle="Выберите датасет, чтобы открыть таблицы с деталями и данными."
          actions={
            <Button
              type="button"
              size="icon"
              variant="secondary"
              onClick={() => setCreateOpen((value) => !value)}
              className="h-8 w-8 border border-border/80 bg-panel-subtle text-foreground transition hover:border-white hover:bg-white hover:text-black hover:shadow-[0_0_14px_rgba(255,255,255,0.6)]"
            >
              <Plus className="h-4 w-4" />
            </Button>
          }
          contentClassName="p-0"
        >
          <div className="divide-y divide-border/80">
            {filteredDatasets.map((dataset) => {
              const isSelected = dataset.id === selectedDatasetId;

              return (
                <button
                  key={dataset.id}
                  type="button"
                  onClick={() => setSelectedDatasetId(dataset.id)}
                  className={cn(
                    "w-full px-4 py-3 text-left transition",
                    isSelected
                      ? "bg-[linear-gradient(135deg,rgba(43,213,118,0.22),rgba(111,247,163,0.12))]"
                      : "hover:bg-panel-subtle"
                  )}
                >
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <div className="text-sm font-medium text-foreground">{dataset.name}</div>
                      <div className="mt-1 text-xs text-muted-foreground">
                        {dataset.timeframe} • {dataset.symbols.join(", ")}
                      </div>
                      <div className="mt-1 text-xs text-muted-foreground">{dataset.period}</div>
                    </div>
                    <div className="flex flex-col items-end gap-1">
                      <Badge className={loadStatusMeta[dataset.loadStatus].className}>
                        {loadStatusMeta[dataset.loadStatus].label}
                      </Badge>
                      {dataset.archived ? <Badge variant="secondary">Архив</Badge> : null}
                      <Badge variant="secondary">{sourceLabels[dataset.source]}</Badge>
                      <Badge variant="secondary">{marketTypeLabels[dataset.marketType]}</Badge>
                    </div>
                  </div>
                </button>
              );
            })}
            {isLoadingDatasets ? (
              <div className="p-4 text-xs text-muted-foreground">
                Загрузка датасетов...
              </div>
            ) : filteredDatasets.length === 0 ? (
              <div className="p-4 text-xs text-muted-foreground">
                По текущим фильтрам датасеты не найдены.
              </div>
            ) : null}
          </div>
        </SurfaceCard>

              {createOpen ? (
        <SurfaceCard
          title="Добавление датасета"
          subtitle="Источник: Binance, Bybit, MOEX или локальные файлы. Для API-источников выберите режим загрузки, для CSV можно выбрать merge/отдельно."
          overflow="visible"
          contentClassName="p-5 pb-6"
          actions={
            <Button
              variant="secondary"
              size="sm"
              onClick={() => resetCreateForm()}
            >
              Сбросить
            </Button>
          }
        >
          <div className="mb-4 rounded-[20px] border border-border/70 bg-[linear-gradient(135deg,rgba(23,39,78,0.35),rgba(18,25,36,0.92)_68%)] p-4">
            <div className="mb-3 text-sm font-semibold text-foreground">Шаблоны импорта</div>
            <div className="grid gap-3 lg:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_auto]">
              <div>
                <div className="mb-1 text-xs text-muted-foreground">Выбрать пресет</div>
                <Select value={selectedTemplateId} onValueChange={handleTemplateSelect}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="none">Без шаблона</SelectItem>
                    {importTemplates.map((template) => (
                      <SelectItem key={template.id} value={template.id}>
                        {template.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div>
                <div className="mb-1 text-xs text-muted-foreground">Имя нового пресета</div>
                <Input
                  value={templateNameDraft}
                  onChange={(event) => setTemplateNameDraft(event.target.value)}
                  placeholder="Например: Bybit ETH 1D или MOEX GAZP 1D"
                />
              </div>
              <div className="flex items-end gap-2">
                <Button type="button" size="sm" variant="secondary" onClick={handleSaveImportTemplate}>
                  Сохранить пресет
                </Button>
                <Button
                  type="button"
                  size="sm"
                  variant="secondary"
                  disabled={selectedTemplateId === "none"}
                  onClick={handleDeleteImportTemplate}
                >
                  Удалить
                </Button>
              </div>
            </div>
            {selectedTemplateId !== "none" ? (
              <div className="mt-3 flex flex-wrap gap-2">
                {(() => {
                  const activeTemplate = importTemplates.find(
                    (template) => template.id === selectedTemplateId
                  );
                  if (!activeTemplate) {
                    return null;
                  }
                  return (
                    <>
                      <Badge variant="secondary">
                        {activeTemplate.source === "local" ? "CSV Upload" : sourceLabels[activeTemplate.source]}
                      </Badge>
                      <Badge variant="secondary">{activeTemplate.marketType}</Badge>
                      <Badge variant="secondary">{activeTemplate.timeframe}</Badge>
                      <Badge variant="secondary">
                        {activeTemplate.source !== "local" &&
                        activeTemplate.binanceLoadMode === "latest"
                          ? `Последние ${activeTemplate.recentCandles || "N"} свечей`
                          : activeTemplate.dateFrom && activeTemplate.dateTo
                            ? `${activeTemplate.dateFrom} -> ${activeTemplate.dateTo}`
                            : "Период не задан"}
                      </Badge>
                    </>
                  );
                })()}
              </div>
            ) : null}
          </div>

          <div
            className={cn(
              "grid gap-4",
              isExchangeForm ? "lg:grid-cols-2" : "lg:grid-cols-1"
            )}
          >
            <div className="space-y-3">
              <div>
                <div className="mb-1 text-xs text-muted-foreground">Название датасета</div>
                <Input
                  value={datasetName}
                  onChange={(event) => setDatasetName(event.target.value)}
                  placeholder="Например: Binance BTCUSDT 1h / Bybit BTCUSDT 5m / MOEX GAZP 1d"
                />
              </div>

              <div>
                <div className="mb-1 text-xs text-muted-foreground">Источник данных</div>
                <Select
                  value={datasetSource}
                  onValueChange={(value) => setDatasetSource(value as DatasetSource)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="binance">Binance (парсинг через API)</SelectItem>
                    <SelectItem value="bybit">Bybit (парсинг через API)</SelectItem>
                    <SelectItem value="moex">MOEX (парсинг через API)</SelectItem>
                    <SelectItem value="local">Локально (CSV upload)</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            {isExchangeForm ? (
              <div className="space-y-3">
                <div>
                  <div className="mb-1 text-xs text-muted-foreground">
                    {datasetSource === "moex" ? "Категория рынка" : "Рынок"}
                  </div>
                  <Select
                    value={marketType}
                    onValueChange={(value) => setMarketType(value as MarketType)}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {getMarketTypeOptions(datasetSource).map((option) => (
                        <SelectItem key={option} value={option}>
                          {marketTypeLabels[option]}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div>
                  <div className="mb-1 text-xs text-muted-foreground">Символы (через запятую)</div>
                  <Input
                    value={symbolsInput}
                    onChange={(event) => setSymbolsInput(event.target.value)}
                    placeholder="BTCUSDT, ETHUSDT"
                  />
                </div>
                <div>
                  <div className="mb-1 text-xs text-muted-foreground">Режим загрузки</div>
                  <Select
                    value={binanceLoadMode}
                    onValueChange={(value) => setBinanceLoadMode(value as BinanceLoadMode)}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="latest">Режим 1: последние N свечей</SelectItem>
                      <SelectItem value="range">Режим 2: диапазон from/to</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                {binanceLoadMode === "latest" ? (
                  <div>
                    <div className="mb-1 text-xs text-muted-foreground">Количество свечей (N)</div>
                    <Input
                      type="number"
                      min={1}
                      step={1}
                      value={recentCandles}
                      onChange={(event) => setRecentCandles(event.target.value)}
                      placeholder="500"
                    />
                  </div>
                ) : (
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <div className="mb-1 text-xs text-muted-foreground">Период from</div>
                      <Input
                        type="date"
                        value={dateFrom}
                        onChange={(event) => setDateFrom(event.target.value)}
                      />
                    </div>
                    <div>
                      <div className="mb-1 text-xs text-muted-foreground">Период to</div>
                      <Input
                        type="date"
                        value={dateTo}
                        onChange={(event) => setDateTo(event.target.value)}
                      />
                    </div>
                  </div>
                )}

                <div>
                  <div className="mb-1 text-xs text-muted-foreground">Таймфрейм</div>
                  <Select
                    value={timeframe}
                    onValueChange={(value) => setTimeframe(value as UiTimeframe)}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {exchangeTimeframes[datasetSource as ExchangeSource].map((item) => (
                        <SelectItem key={item} value={item}>
                          {item}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>
            ) : null}
          </div>

          {datasetSource === "local" ? (
            <div className="mt-5 space-y-3 rounded-[18px] border border-border bg-panel-subtle p-4">
              <div className="text-sm font-medium text-foreground">Локальные CSV файлы</div>
              <Input type="file" accept=".csv,text/csv" multiple onChange={handleCsvFileChange} />
              {uploadedCsvFiles.length > 0 ? (
                <div className="text-xs text-muted-foreground">
                  Выбрано файлов: {uploadedCsvFiles.length}, общий размер: {formatBytes(
                    uploadedCsvFiles.reduce((total, file) => total + file.size, 0)
                  )}
                </div>
              ) : null}
            </div>
          ) : null}

          {datasetSource === "local" && csvValidation ? (
            <div
              className={cn(
                "mt-3 rounded-[16px] border p-4 text-xs",
                csvValidation.isValid
                  ? "border-status-success/40 bg-status-success/10 text-status-success"
                  : "border-status-error/40 bg-status-error/10 text-status-error"
              )}
            >
              <div className="mb-2 flex items-center justify-between gap-2 text-sm font-medium">
                <span>Валидация CSV</span>
                <span>{csvValidation.isValid ? "Прошла успешно" : "Есть ошибки"}</span>
              </div>
              <div className="grid gap-1 text-xs">
                <div>Колонки: {csvValidation.detectedColumns.join(", ") || "не определены"}</div>
                <div>Отсутствуют обязательные: {csvValidation.missingColumns.join(", ") || "нет"}</div>
                <div>Дубликаты timestamp: {csvValidation.duplicateTimestampCount}</div>
                <div>Пропуски значений: {csvValidation.missingValueCount}</div>
                <div>Нарушения сортировки: {csvValidation.unsortedCount}</div>
              </div>
            </div>
          ) : null}

          {datasetSource === "local" && csvImportIssues.length > 0 ? (
            <div className="mt-4 rounded-[18px] border border-status-error/40 bg-[linear-gradient(145deg,rgba(120,34,34,0.16),rgba(28,18,18,0.55)_72%)] p-4">
              <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
                <div>
                  <div className="text-sm font-semibold text-foreground">Превью ошибок импорта</div>
                  <div className="text-xs text-muted-foreground">
                    Проблемных строк: {csvImportIssues.length}
                  </div>
                </div>
                <Button type="button" size="sm" variant="secondary" onClick={handleExportImportIssues}>
                  <Download className="mr-2 h-4 w-4" />
                  Экспорт отчета
                </Button>
              </div>

              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Файл</TableHead>
                    <TableHead>Строка</TableHead>
                    <TableHead>Timestamp</TableHead>
                    <TableHead>Поле</TableHead>
                    <TableHead>Ошибка</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {csvImportIssues.slice(0, 25).map((issue, index) => (
                    <TableRow key={`${issue.fileName}-${issue.rowNumber}-${issue.issue}-${index}`}>
                      <TableCell className="text-xs text-muted-foreground">{issue.fileName}</TableCell>
                      <TableCell className="text-xs text-muted-foreground">{issue.rowNumber}</TableCell>
                      <TableCell className="text-xs text-muted-foreground">{issue.timestamp}</TableCell>
                      <TableCell className="text-xs text-muted-foreground">{issue.column}</TableCell>
                      <TableCell className="text-xs text-muted-foreground">
                        {importIssueLabels[issue.issue]}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
              {csvImportIssues.length > 25 ? (
                <div className="mt-2 text-xs text-muted-foreground">
                  Показаны первые 25 строк. Полный список выгрузите кнопкой выше.
                </div>
              ) : null}
            </div>
          ) : null}

          {shouldAskLocalCsvMode ? (
            <div className="mt-4 rounded-[18px] border border-border bg-panel-subtle p-4">
              <div className="text-sm font-medium text-foreground">
                Загружено несколько CSV файлов
              </div>
              <div className="mt-1 text-xs text-muted-foreground">
                Выберите режим: смёрджить в один датасет или оставить файлы по отдельности.
              </div>
              <div className="mt-3 flex flex-wrap gap-2">
                <Button
                  type="button"
                  size="sm"
                  variant={localCsvMode === "merge" ? "default" : "secondary"}
                  onClick={() => {
                    setLocalCsvMode("merge");
                    setMergeError(null);
                  }}
                >
                  Смёрджить
                </Button>
                <Button
                  type="button"
                  size="sm"
                  variant={localCsvMode === "separate" ? "default" : "secondary"}
                  onClick={() => {
                    setLocalCsvMode("separate");
                    setMergeError(null);
                    if (mergedCsv?.url) {
                      URL.revokeObjectURL(mergedCsv.url);
                    }
                    setMergedCsv(null);
                  }}
                >
                  По отдельности
                </Button>
              </div>
            </div>
          ) : null}

          {showMergePanel ? (
            <div className="mt-4 space-y-3 rounded-[18px] border border-border bg-panel-subtle p-4">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <div className="text-sm font-medium text-foreground">Склейка CSV</div>
                  <div className="mt-1 text-xs text-muted-foreground">
                    Загрузите несколько CSV, объедините в один файл и используйте его как датасет.
                  </div>
                </div>
                <Button type="button" size="sm" variant="secondary" onClick={handleMergeCsv}>
                  <UploadCloud className="mr-2 h-4 w-4" />
                  Склеить CSV
                </Button>
              </div>

              {mergedCsv ? (
                <div className="flex flex-wrap items-center gap-3 text-xs">
                  <Badge variant="secondary">{mergedCsv.rows.toLocaleString("ru-RU")} строк</Badge>
                  <Badge variant="secondary">{formatBytes(mergedCsv.size)}</Badge>
                  <a
                    href={mergedCsv.url}
                    download={mergedCsv.name}
                    className="inline-flex items-center text-primary hover:underline"
                  >
                    <Download className="mr-1 h-3.5 w-3.5" />
                    Скачать merged CSV
                  </a>
                </div>
              ) : null}
            </div>
          ) : null}

          {datasetSource === "local" &&
          uploadedCsvFiles.length > 1 &&
          localCsvMode === "separate" ? (
            <div className="mt-4 rounded-[14px] border border-border bg-panel-subtle p-3 text-xs text-muted-foreground">
              Файлы будут добавлены по отдельности в рамках одного локального датасета (без merge).
            </div>
          ) : null}

          {mergeError ? <div className="mt-3 text-xs text-status-failed">{mergeError}</div> : null}

          <div className="mt-5 flex justify-end">
            <Button type="button" onClick={handleAddDataset} disabled={isImportingCandles}>
              {isImportingCandles ? "Импорт свечей..." : "Добавить датасет в список"}
            </Button>
          </div>
        </SurfaceCard>
      ) : null}

        {!createOpen ? (
          <SurfaceCard
          title={selectedDataset ? `Таблицы: ${selectedDataset.name}` : "Таблицы датасета"}
          subtitle={
            selectedDataset
              ? "Метаданные и первые строки выбранного датасета."
              : undefined
          }
        >
          {selectedDataset ? (
            <div className="space-y-4">
              <div className="rounded-[18px] border border-border bg-panel-subtle p-4">
                <div className="mb-3 text-sm font-semibold text-foreground">Действия с датасетом</div>
                <div className="grid gap-3 lg:grid-cols-[minmax(0,1fr)_auto_auto]">
                  <Input
                    value={renameDraft}
                    onChange={(event) => setRenameDraft(event.target.value)}
                    placeholder="Новое имя датасета"
                  />
                  <Button type="button" size="sm" variant="secondary" onClick={handleRenameDataset}>
                    Переименовать
                  </Button>
                  <Button type="button" size="sm" variant="secondary" onClick={handleDuplicateDataset}>
                    Дублировать
                  </Button>
                </div>
              </div>

              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Параметр</TableHead>
                    <TableHead>Значение</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  <TableRow>
                    <TableCell className="text-xs text-muted-foreground">Источник</TableCell>
                    <TableCell className="text-xs text-muted-foreground">
                      {sourceLabels[selectedDataset.source]}
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell className="text-xs text-muted-foreground">Тип рынка</TableCell>
                    <TableCell className="text-xs text-muted-foreground">
                      {marketTypeLabels[selectedDataset.marketType]}
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell className="text-xs text-muted-foreground">Статус загрузки</TableCell>
                    <TableCell className="text-xs text-muted-foreground">
                      <Badge className={loadStatusMeta[selectedDataset.loadStatus].className}>
                        {loadStatusMeta[selectedDataset.loadStatus].label}
                      </Badge>
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell className="text-xs text-muted-foreground">Период</TableCell>
                    <TableCell className="text-xs text-muted-foreground">{selectedDataset.period}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell className="text-xs text-muted-foreground">Покрытие времени</TableCell>
                    <TableCell className="text-xs text-muted-foreground">
                      {getDatasetCoverageLabel(selectedDataset)}
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell className="text-xs text-muted-foreground">Таймфрейм</TableCell>
                    <TableCell className="text-xs text-muted-foreground">{selectedDataset.timeframe}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell className="text-xs text-muted-foreground">Символы</TableCell>
                    <TableCell className="text-xs text-muted-foreground">
                      {selectedDataset.symbols.join(", ")}
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell className="text-xs text-muted-foreground">Размер</TableCell>
                    <TableCell className="text-xs text-muted-foreground">{selectedDataset.size}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell className="text-xs text-muted-foreground">Статус</TableCell>
                    <TableCell className="text-xs text-muted-foreground">
                      {selectedDataset.rowsHint ?? "Готов"}
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell className="text-xs text-muted-foreground">Версия dataset</TableCell>
                    <TableCell className="font-mono text-xs text-muted-foreground">
                      {selectedDataset.datasetVersion ?? "N/A"}
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell className="text-xs text-muted-foreground">Snapshot</TableCell>
                    <TableCell className="font-mono text-xs text-muted-foreground">
                      {selectedDataset.latestSnapshotId ?? "N/A"}
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell className="text-xs text-muted-foreground">Quality</TableCell>
                    <TableCell className="text-xs text-muted-foreground">
                      <div className="flex flex-wrap items-center gap-2">
                        <Badge variant="secondary">{selectedDataset.qualityStatus ?? "N/A"}</Badge>
                        <span>{selectedDataset.qualityIssueCount ?? 0} issues</span>
                      </div>
                    </TableCell>
                  </TableRow>
                  {selectedDataset.backendRequest ? (
                    <TableRow>
                      <TableCell className="text-xs text-muted-foreground">API запрос</TableCell>
                      <TableCell className="text-xs text-muted-foreground">
                        {selectedDataset.backendRequest.exchange} / {selectedDataset.backendRequest.interval} /{" "}
                        {selectedDataset.backendRequest.from}
                        {" -> "}
                        {selectedDataset.backendRequest.to}
                      </TableCell>
                    </TableRow>
                  ) : null}
                </TableBody>
              </Table>

              <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
                <div className="rounded-[16px] border border-border bg-panel-subtle p-3">
                  <div className="text-[11px] uppercase tracking-[0.14em] text-muted-foreground">
                    Профиль: строки
                  </div>
                  <div className="mt-1 text-sm font-medium text-foreground">
                    {selectedDataset.profile.rowCount}
                  </div>
                </div>
                <div className="rounded-[16px] border border-border bg-panel-subtle p-3">
                  <div className="text-[11px] uppercase tracking-[0.14em] text-muted-foreground">
                    Диапазон дат
                  </div>
                  <div className="mt-1 text-sm font-medium text-foreground">
                    {selectedDataset.profile.dateRange}
                  </div>
                </div>
                <div className="rounded-[16px] border border-border bg-panel-subtle p-3">
                  <div className="text-[11px] uppercase tracking-[0.14em] text-muted-foreground">
                    Шаг времени
                  </div>
                  <div className="mt-1 text-sm font-medium text-foreground">
                    {selectedDataset.profile.timeStep}
                  </div>
                </div>
                <div className="rounded-[16px] border border-border bg-panel-subtle p-3">
                  <div className="text-[11px] uppercase tracking-[0.14em] text-muted-foreground">
                    Покрытие символов
                  </div>
                  <div className="mt-1 text-sm font-medium text-foreground">
                    {selectedDataset.profile.symbolCoverage}
                  </div>
                </div>
              </div>

              <div className="rounded-[18px] border border-border bg-panel-subtle p-4">
                <div className="mb-2 text-sm font-semibold text-foreground">
                  Проверка совместимости с бэктестом
                </div>
                <div className="mb-3">
                  <Badge
                    className={
                      selectedDataset.compatibility.compatible
                        ? "border border-status-success/40 bg-status-success/15 text-status-success"
                        : "border border-status-error/40 bg-status-error/15 text-status-error"
                    }
                  >
                    {selectedDataset.compatibility.compatible
                      ? "Подходит для бэктеста"
                      : "Неполный формат"}
                  </Badge>
                </div>
                <div className="space-y-1 text-xs text-muted-foreground">
                  <div>
                    Отсутствующие поля:{" "}
                    {selectedDataset.compatibility.missingFields.join(", ") || "нет"}
                  </div>
                  {selectedDataset.compatibility.notes.map((note) => (
                    <div key={note}>• {note}</div>
                  ))}
                </div>
              </div>

              <div className="flex flex-wrap items-center gap-3 rounded-[14px] border border-border bg-panel-subtle p-3 text-xs">
                {selectedDataset.mergedCsvUrl ? <Badge variant="secondary">Merged CSV</Badge> : null}
                {selectedDataset.mergedCsvUrl ? (
                  <span className="text-muted-foreground">
                    {selectedDataset.mergedCsvRows?.toLocaleString("ru-RU") ?? 0} строк
                  </span>
                ) : null}
                <div className="ml-auto flex flex-wrap gap-2">
                  {selectedDataset.mergedCsvUrl ? (
                    <a
                      href={selectedDataset.mergedCsvUrl}
                      download={selectedDataset.mergedCsvName}
                      className="inline-flex items-center rounded-full border border-white/10 bg-[linear-gradient(145deg,rgba(43,213,118,0.12),rgba(111,247,163,0.08))] px-3 py-1.5 text-xs text-secondary-foreground transition hover:border-[rgba(92,240,158,0.24)] hover:shadow-[0_0_20px_rgba(43,213,118,0.14)]"
                    >
                      <Download className="mr-1.5 h-3.5 w-3.5" />
                      Экспорт данных
                    </a>
                  ) : null}
                  <Button type="button" size="sm" variant="destructive" onClick={handleDeleteDataset}>
                    Удалить
                  </Button>
                </div>
              </div>

              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Временная метка</TableHead>
                    <TableHead>Символ</TableHead>
                    <TableHead>Открытие</TableHead>
                    <TableHead>Макс.</TableHead>
                    <TableHead>Мин.</TableHead>
                    <TableHead>Закрытие</TableHead>
                    <TableHead>Объем</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {selectedDatasetRows.map((row) => (
                    <TableRow key={`${selectedDataset.id}-${row.symbol}-${row.ts}`}>
                      <TableCell className="text-xs text-muted-foreground">{row.ts}</TableCell>
                      <TableCell className="text-xs text-muted-foreground">{row.symbol}</TableCell>
                      <TableCell className="text-xs text-muted-foreground">{row.open}</TableCell>
                      <TableCell className="text-xs text-muted-foreground">{row.high}</TableCell>
                      <TableCell className="text-xs text-muted-foreground">{row.low}</TableCell>
                      <TableCell className="text-xs text-muted-foreground">{row.close}</TableCell>
                      <TableCell className="text-xs text-muted-foreground">{row.volume}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          ) : (
            <div className="rounded-[18px] border border-dashed border-border bg-panel-subtle p-6 text-sm text-muted-foreground">
              Выберите датасет слева, чтобы открыть таблицы с параметрами и данными.
            </div>
          )}
          </SurfaceCard>
        ) : null}
      </div>
    </div>
  );
}
