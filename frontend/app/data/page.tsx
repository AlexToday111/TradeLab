"use client";

import { ChangeEvent, useEffect, useMemo, useState } from "react";
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
  dataQuality,
  dataSources,
  datasetVersions,
  previewRows,
  type DatasetVersion,
} from "@/lib/demo-data/datasets";
import { getDataSourceStatusLabel, getDataSourceTypeLabel } from "@/lib/ui-text";
import { cn } from "@/lib/utils";

type DatasetSource = "bybit" | "local";
type MarketType = "spot" | "futures";
type LocalCsvMode = "merge" | "separate";
type DatasetLoadStatus = "queued" | "processing" | "ready" | "error";

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

type DatasetProfile = {
  rowCount: string;
  dateRange: string;
  timeStep: string;
  symbolCoverage: string;
};

type UiDataset = DatasetVersion & {
  source: DatasetSource;
  marketType: MarketType;
  loadStatus: DatasetLoadStatus;
  profile: DatasetProfile;
  rowsHint?: string;
  mergedCsvUrl?: string;
  mergedCsvName?: string;
  mergedCsvRows?: number;
};

type MergedCsv = {
  url: string;
  name: string;
  rows: number;
  size: number;
};

const sourceLabels: Record<DatasetSource, string> = {
  bybit: "ByBit",
  local: "Локально",
};

const marketTypeLabels: Record<MarketType, string> = {
  spot: "Spot",
  futures: "Futures",
};

const bybitTimeframes = ["5M", "1H", "1D"] as const;
const requiredCsvColumns = ["timestamp", "open", "high", "low", "close", "volume"];

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

function parseCsvRow(line: string) {
  return line.split(",").map((cell) => cell.trim());
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

async function validateCsvFiles(files: File[]): Promise<CsvValidation | null> {
  if (files.length === 0) {
    return null;
  }

  let headerColumns: string[] = [];
  const timestampSeen = new Set<string>();

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
        }
      }

      if (timestampIndex !== -1) {
        const timestampValue = cells[timestampIndex];
        if (timestampValue) {
          if (timestampSeen.has(timestampValue)) {
            duplicateTimestampCount += 1;
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
    dateRangeStart: minTimestampValue ? new Date(minTimestampValue).toISOString().slice(0, 10) : null,
    dateRangeEnd: maxTimestampValue ? new Date(maxTimestampValue).toISOString().slice(0, 10) : null,
    inferredStep: formatTimeStep(averageStep ? Math.round(averageStep) : null),
    symbolCoverage:
      distinctSymbols.size > 0
        ? `${distinctSymbols.size} символов`
        : "N/A",
  };
}

function createInitialDatasets(): UiDataset[] {
  const demoRange = `${previewRows[0]?.ts ?? "N/A"} -> ${previewRows[previewRows.length - 1]?.ts ?? "N/A"}`;
  return datasetVersions.map((dataset) => ({
    ...dataset,
    source: "local",
    marketType: "spot",
    loadStatus: "ready",
    profile: {
      rowCount: `${previewRows.length} строк`,
      dateRange: demoRange,
      timeStep: dataset.timeframe,
      symbolCoverage: `${dataset.symbols.length} символов`,
    },
    rowsHint: "Демо-набор",
  }));
}

export default function DataPage() {
  const [datasets, setDatasets] = useState<UiDataset[]>(createInitialDatasets);
  const [selectedDatasetId, setSelectedDatasetId] = useState<string | null>(null);

  const [createOpen, setCreateOpen] = useState(false);
  const [datasetName, setDatasetName] = useState("");
  const [datasetSource, setDatasetSource] = useState<DatasetSource>("bybit");
  const [marketType, setMarketType] = useState<MarketType>("spot");
  const [symbolsInput, setSymbolsInput] = useState("BTCUSDT");
  const [timeframe, setTimeframe] = useState<(typeof bybitTimeframes)[number]>("1H");
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [uploadedCsvFiles, setUploadedCsvFiles] = useState<File[]>([]);
  const [csvValidation, setCsvValidation] = useState<CsvValidation | null>(null);
  const [localCsvMode, setLocalCsvMode] = useState<LocalCsvMode | null>(null);
  const [mergeError, setMergeError] = useState<string | null>(null);
  const [mergedCsv, setMergedCsv] = useState<MergedCsv | null>(null);

  const selectedDataset = useMemo(
    () => datasets.find((dataset) => dataset.id === selectedDatasetId) ?? null,
    [datasets, selectedDatasetId]
  );

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
    setDatasetSource("bybit");
    setMarketType("spot");
    setSymbolsInput("BTCUSDT");
    setTimeframe("1H");
    setDateFrom("");
    setDateTo("");
    setUploadedCsvFiles([]);
    setCsvValidation(null);
    setLocalCsvMode(null);
    setMergeError(null);
    setMergedCsv(null);
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

    const validation = await validateCsvFiles(files);
    setCsvValidation(validation);
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

  function handleAddDataset() {
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

    const symbols = symbolsInput
      .split(/[\s,]+/)
      .map((symbol) => symbol.trim())
      .filter(Boolean);

    const uploadedSize = uploadedCsvFiles.reduce((total, file) => total + file.size, 0);

    const datasetId = `custom-${Date.now()}`;
    const isLocal = datasetSource === "local";
    const isMergeMode = isLocal && localCsvMode === "merge";
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
            dateRange:
              dateFrom && dateTo ? `${dateFrom} -> ${dateTo}` : "Ожидается",
            timeStep: timeframe,
            symbolCoverage: `${symbols.length} символов`,
          };
    const newDataset: UiDataset = {
      id: datasetId,
      name: datasetName.trim() || `Новый датасет ${datasets.length + 1}`,
      period: isLocal
        ? "Локальный импорт"
        : dateFrom && dateTo
          ? `${dateFrom} -> ${dateTo}`
          : "Импорт из ByBit",
      timeframe: isLocal ? "N/A" : timeframe,
      symbols: isLocal ? ["CSV"] : symbols.length > 0 ? symbols : ["N/A"],
      size: isMergeMode && mergedCsv ? formatBytes(mergedCsv.size) : formatBytes(uploadedSize),
      pipelineHash: isMergeMode ? "csv_merge_local" : "dataset_pending",
      source: datasetSource,
      marketType: isLocal ? "spot" : marketType,
      loadStatus: datasetSource === "bybit" ? "queued" : "ready",
      profile,
      rowsHint: isMergeMode && mergedCsv
        ? `${mergedCsv.rows.toLocaleString("ru-RU")} строк (merged)`
        : datasetSource === "bybit"
          ? "Ожидает парсинг"
          : uploadedCsvFiles.length > 1
            ? `${uploadedCsvFiles.length} файл(ов) по отдельности`
            : uploadedCsvFiles.length > 0
              ? "1 CSV файл"
            : "Черновик",
      mergedCsvName: isMergeMode ? mergedCsv?.name : undefined,
      mergedCsvRows: isMergeMode ? mergedCsv?.rows : undefined,
      mergedCsvUrl: isMergeMode ? mergedCsv?.url : undefined,
    };

    setDatasets((prev) => [newDataset, ...prev]);
    setSelectedDatasetId(datasetId);
    setCreateOpen(false);
    resetCreateForm({ preserveMergedCsv: Boolean(mergedCsv) });
  }

  const isBybitForm = datasetSource === "bybit";
  const shouldAskLocalCsvMode =
    datasetSource === "local" && uploadedCsvFiles.length > 1;
  const showMergePanel = datasetSource === "local" && localCsvMode === "merge";

  return (
    <div className="flex h-full flex-col gap-5">
      <PageHeader
        eyebrow="Данные"
        title="Данные"
        description="Выберите датасет из списка, затем работайте с его таблицами."
        actions={
          <Button size="sm" onClick={() => setCreateOpen((value) => !value)}>
            <Plus className="mr-2 h-4 w-4" />
            {createOpen ? "Свернуть форму" : "Добавить датасет"}
          </Button>
        }
      />

      {createOpen ? (
        <SurfaceCard
          title="Добавление датасета"
          subtitle="Источник: ByBit или локальные файлы. Для нескольких CSV можно выбрать режим: смёрджить или по отдельности."
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
          <div
            className={cn(
              "grid gap-4",
              isBybitForm ? "lg:grid-cols-2" : "lg:grid-cols-1"
            )}
          >
            <div className="space-y-3">
              <div>
                <div className="mb-1 text-xs text-muted-foreground">Название датасета</div>
                <Input
                  value={datasetName}
                  onChange={(event) => setDatasetName(event.target.value)}
                  placeholder="Например: ByBit BTCUSDT 1h"
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
                    <SelectItem value="bybit">ByBit (парсинг через API)</SelectItem>
                    <SelectItem value="local">Локально (CSV upload)</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            {isBybitForm ? (
              <div className="space-y-3">
              <div>
                <div className="mb-1 text-xs text-muted-foreground">Рынок</div>
                <Select
                  value={marketType}
                  onValueChange={(value) => setMarketType(value as MarketType)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="spot">Spot</SelectItem>
                    <SelectItem value="futures">Futures</SelectItem>
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

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <div className="mb-1 text-xs text-muted-foreground">Период от</div>
                  <Input
                    type="date"
                    value={dateFrom}
                    onChange={(event) => setDateFrom(event.target.value)}
                  />
                </div>
                <div>
                  <div className="mb-1 text-xs text-muted-foreground">Период до</div>
                  <Input
                    type="date"
                    value={dateTo}
                    onChange={(event) => setDateTo(event.target.value)}
                  />
                </div>
              </div>

              <div>
                <div className="mb-1 text-xs text-muted-foreground">Таймфрейм</div>
                <Select
                  value={timeframe}
                  onValueChange={(value) =>
                    setTimeframe(value as (typeof bybitTimeframes)[number])
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {bybitTimeframes.map((item) => (
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
            <Button type="button" onClick={handleAddDataset}>
              Добавить датасет в список
            </Button>
          </div>
        </SurfaceCard>
      ) : null}

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        {dataSources.map((source) => (
          <SurfaceCard
            key={source.id}
            className="py-0"
            contentClassName="flex items-center justify-between p-4"
          >
            <div className="flex items-center gap-3">
              <div className="rounded-[14px] border border-border bg-panel-subtle p-2">
                <Database className="h-4 w-4 text-muted-foreground" />
              </div>
              <div>
                <div className="text-sm font-medium text-foreground">{source.name}</div>
                <div className="text-xs text-muted-foreground">{getDataSourceTypeLabel(source.type)}</div>
              </div>
            </div>
            <Badge
              className={
                source.status === "connected"
                  ? "border border-status-success/40 bg-status-success/20 text-status-success"
                  : "border border-border bg-secondary text-muted-foreground"
              }
            >
              {getDataSourceStatusLabel(source.status)}
            </Badge>
          </SurfaceCard>
        ))}
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-[360px_minmax(0,1fr)]">
        <SurfaceCard
          title="Список датасетов"
          subtitle="Выберите датасет, чтобы открыть таблицы с деталями и данными."
          contentClassName="p-0"
        >
          <div className="divide-y divide-border/80">
            {datasets.map((dataset) => {
              const isSelected = dataset.id === selectedDatasetId;

              return (
                <button
                  key={dataset.id}
                  type="button"
                  onClick={() => setSelectedDatasetId(dataset.id)}
                  className={cn(
                    "w-full px-4 py-3 text-left transition",
                    isSelected ? "bg-secondary/60" : "hover:bg-panel-subtle"
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
                      <Badge variant="secondary">{sourceLabels[dataset.source]}</Badge>
                      <Badge variant="secondary">{marketTypeLabels[dataset.marketType]}</Badge>
                    </div>
                  </div>
                </button>
              );
            })}
          </div>
        </SurfaceCard>

        <SurfaceCard
          title={selectedDataset ? `Таблицы: ${selectedDataset.name}` : "Таблицы датасета"}
          subtitle={
            selectedDataset
              ? "Метаданные и первые строки выбранного датасета."
              : "Сначала выберите датасет слева."
          }
        >
          {selectedDataset ? (
            <div className="space-y-4">
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

              {selectedDataset.mergedCsvUrl ? (
                <div className="flex flex-wrap items-center gap-3 rounded-[14px] border border-border bg-panel-subtle p-3 text-xs">
                  <Badge variant="secondary">Merged CSV</Badge>
                  <span className="text-muted-foreground">
                    {selectedDataset.mergedCsvRows?.toLocaleString("ru-RU") ?? 0} строк
                  </span>
                  <a
                    href={selectedDataset.mergedCsvUrl}
                    download={selectedDataset.mergedCsvName}
                    className="inline-flex items-center text-primary hover:underline"
                  >
                    <Download className="mr-1 h-3.5 w-3.5" />
                    Скачать файл
                  </a>
                </div>
              ) : null}

              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Временная метка</TableHead>
                    <TableHead>Открытие</TableHead>
                    <TableHead>Макс.</TableHead>
                    <TableHead>Мин.</TableHead>
                    <TableHead>Закрытие</TableHead>
                    <TableHead>Объем</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {previewRows.map((row) => (
                    <TableRow key={`${selectedDataset.id}-${row.ts}`}>
                      <TableCell className="text-xs text-muted-foreground">{row.ts}</TableCell>
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
      </div>

      <SurfaceCard title="Качество данных">
        <div className="grid grid-cols-2 gap-3 text-xs text-muted-foreground md:grid-cols-4">
          {dataQuality.map((item) => (
            <div key={item.label} className="rounded-[18px] border border-border bg-panel-subtle p-3">
              <div className="text-[11px] uppercase">{item.label}</div>
              <div className="text-foreground">{item.value}</div>
            </div>
          ))}
        </div>
      </SurfaceCard>
    </div>
  );
}
