"use client";

import { ChangeEvent, useMemo, useState } from "react";
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

type UiDataset = DatasetVersion & {
  source: DatasetSource;
  marketType: MarketType;
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

function createInitialDatasets(): UiDataset[] {
  return datasetVersions.map((dataset) => ({
    ...dataset,
    source: "local",
    marketType: "spot",
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
  const [timeframe, setTimeframe] = useState("1h");
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [uploadedCsvFiles, setUploadedCsvFiles] = useState<File[]>([]);
  const [mergeError, setMergeError] = useState<string | null>(null);
  const [mergedCsv, setMergedCsv] = useState<MergedCsv | null>(null);

  const selectedDataset = useMemo(
    () => datasets.find((dataset) => dataset.id === selectedDatasetId) ?? null,
    [datasets, selectedDatasetId]
  );

  function resetCreateForm(options?: { preserveMergedCsv?: boolean }) {
    if (!options?.preserveMergedCsv && mergedCsv?.url) {
      URL.revokeObjectURL(mergedCsv.url);
    }

    setDatasetName("");
    setDatasetSource("bybit");
    setMarketType("spot");
    setSymbolsInput("BTCUSDT");
    setTimeframe("1h");
    setDateFrom("");
    setDateTo("");
    setUploadedCsvFiles([]);
    setMergeError(null);
    setMergedCsv(null);
  }

  function handleCsvFileChange(event: ChangeEvent<HTMLInputElement>) {
    const files = event.target.files ? Array.from(event.target.files) : [];

    if (mergedCsv?.url) {
      URL.revokeObjectURL(mergedCsv.url);
      setMergedCsv(null);
    }

    setUploadedCsvFiles(files);
    setMergeError(null);
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
    const symbols = symbolsInput
      .split(/[\s,]+/)
      .map((symbol) => symbol.trim())
      .filter(Boolean);

    const uploadedSize = uploadedCsvFiles.reduce((total, file) => total + file.size, 0);

    const datasetId = `custom-${Date.now()}`;
    const newDataset: UiDataset = {
      id: datasetId,
      name: datasetName.trim() || `Новый датасет ${datasets.length + 1}`,
      period:
        dateFrom && dateTo
          ? `${dateFrom} -> ${dateTo}`
          : datasetSource === "bybit"
            ? "Импорт из ByBit"
            : "Локальный импорт",
      timeframe,
      symbols: symbols.length > 0 ? symbols : ["N/A"],
      size: mergedCsv ? formatBytes(mergedCsv.size) : formatBytes(uploadedSize),
      pipelineHash: mergedCsv ? "csv_merge_local" : "dataset_pending",
      source: datasetSource,
      marketType,
      rowsHint: mergedCsv
        ? `${mergedCsv.rows.toLocaleString("ru-RU")} строк`
        : datasetSource === "bybit"
          ? "Ожидает парсинг"
          : uploadedCsvFiles.length > 0
            ? `${uploadedCsvFiles.length} файл(ов)`
            : "Черновик",
      mergedCsvName: mergedCsv?.name,
      mergedCsvRows: mergedCsv?.rows,
      mergedCsvUrl: mergedCsv?.url,
    };

    setDatasets((prev) => [newDataset, ...prev]);
    setSelectedDatasetId(datasetId);
    setCreateOpen(false);
    resetCreateForm({ preserveMergedCsv: Boolean(mergedCsv) });
  }

  const showFuturesMerge = datasetSource === "local" && marketType === "futures";

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
          subtitle="Источник: ByBit или локальные файлы. Для фьючерсов поддержана склейка нескольких CSV в один."
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
          <div className="grid gap-4 lg:grid-cols-2">
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
            </div>

            <div className="space-y-3">
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
                  <div className="mb-1 text-xs text-muted-foreground">Таймфрейм</div>
                  <Input
                    value={timeframe}
                    onChange={(event) => setTimeframe(event.target.value)}
                    placeholder="1h"
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
                <div className="mb-1 text-xs text-muted-foreground">Период от</div>
                <Input
                  type="date"
                  value={dateFrom}
                  onChange={(event) => setDateFrom(event.target.value)}
                />
              </div>
            </div>
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

          {showFuturesMerge ? (
            <div className="mt-4 space-y-3 rounded-[18px] border border-border bg-panel-subtle p-4">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <div className="text-sm font-medium text-foreground">Склейка CSV контрактов</div>
                  <div className="mt-1 text-xs text-muted-foreground">
                    Загрузите несколько CSV, объедините в один файл и используйте его как датасет.
                  </div>
                </div>
                <Button type="button" size="sm" variant="secondary" onClick={handleMergeCsv}>
                  <UploadCloud className="mr-2 h-4 w-4" />
                  Склеить CSV
                </Button>
              </div>

              {mergeError ? <div className="text-xs text-status-failed">{mergeError}</div> : null}

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
