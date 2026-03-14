import type { TradeRow } from "@/lib/demo-data/trades";

export const equityCurve = [
  { date: "2023-01", value: 100 },
  { date: "2023-03", value: 104 },
  { date: "2023-05", value: 110 },
  { date: "2023-07", value: 118 },
  { date: "2023-09", value: 116 },
  { date: "2023-11", value: 124 },
  { date: "2024-01", value: 129 },
  { date: "2024-03", value: 134 },
  { date: "2024-05", value: 142 },
  { date: "2024-07", value: 147 },
  { date: "2024-09", value: 151 },
  { date: "2024-12", value: 158 },
];

export const drawdownSeries = [
  { date: "2023-01", value: -1.2 },
  { date: "2023-03", value: -2.4 },
  { date: "2023-05", value: -3.1 },
  { date: "2023-07", value: -2.2 },
  { date: "2023-09", value: -4.8 },
  { date: "2023-11", value: -3.7 },
  { date: "2024-01", value: -2.9 },
  { date: "2024-03", value: -4.1 },
  { date: "2024-05", value: -2.6 },
  { date: "2024-07", value: -1.8 },
  { date: "2024-09", value: -2.5 },
  { date: "2024-12", value: -1.4 },
];

export const underwaterSeries = [
  { date: "2023-01", value: -1.5 },
  { date: "2023-03", value: -3.2 },
  { date: "2023-05", value: -5.6 },
  { date: "2023-07", value: -3.8 },
  { date: "2023-09", value: -7.4 },
  { date: "2023-11", value: -4.1 },
  { date: "2024-01", value: -2.7 },
  { date: "2024-03", value: -6.2 },
  { date: "2024-05", value: -3.4 },
  { date: "2024-07", value: -1.9 },
  { date: "2024-09", value: -2.3 },
  { date: "2024-12", value: -1.2 },
];

export const returnsHistogram = [
  { bucket: "-4%", value: 2 },
  { bucket: "-3%", value: 4 },
  { bucket: "-2%", value: 8 },
  { bucket: "-1%", value: 14 },
  { bucket: "0%", value: 20 },
  { bucket: "1%", value: 18 },
  { bucket: "2%", value: 10 },
  { bucket: "3%", value: 6 },
  { bucket: "4%", value: 3 },
];

export const ohlcPreview = [
  { date: "01-02", value: 487 },
  { date: "01-03", value: 484 },
  { date: "01-04", value: 492 },
  { date: "01-05", value: 489 },
  { date: "01-08", value: 493 },
];

export type DatasetPriceRow = {
  ts: string;
  close: number;
};

export type TradeAnalyzerPoint = {
  date: string;
  close: number;
};

export type TradeMarker = {
  date: string;
  value: number;
  tradeId: string;
  symbol: string;
  side: "Long" | "Short";
  action: "entry" | "exit";
  pnl: number;
};

function formatDateKey(value: Date) {
  return value.toISOString().slice(0, 10);
}

function parseDateKey(value: string) {
  const parsed = Date.parse(value);
  return Number.isNaN(parsed) ? null : new Date(parsed);
}

function buildDailyRange(start: Date, end: Date) {
  const points: string[] = [];
  const cursor = new Date(start);
  while (cursor <= end) {
    points.push(formatDateKey(cursor));
    cursor.setDate(cursor.getDate() + 1);
  }
  return points;
}

export function buildTradeAnalyzerFromDataset(params: {
  datasetRows: DatasetPriceRow[];
  trades: TradeRow[];
}) {
  if (params.trades.length === 0) {
    return {
      priceSeries: [] as TradeAnalyzerPoint[],
      entryMarkers: [] as TradeMarker[],
      exitMarkers: [] as TradeMarker[],
    };
  }

  const sortedDataset = [...params.datasetRows].sort((a, b) => a.ts.localeCompare(b.ts));
  const basePrice = sortedDataset[sortedDataset.length - 1]?.close ?? 100;
  const avgDelta =
    sortedDataset.length > 1
      ? sortedDataset
          .slice(1)
          .reduce((sum, row, index) => sum + (row.close - sortedDataset[index].close), 0) /
        (sortedDataset.length - 1)
      : 0.15;

  const validEntries = params.trades
    .map((trade) => parseDateKey(trade.entry))
    .filter((value): value is Date => value !== null);
  const validExits = params.trades
    .map((trade) => parseDateKey(trade.exit))
    .filter((value): value is Date => value !== null);

  const minEntry = validEntries.reduce((min, date) => (date < min ? date : min), validEntries[0]);
  const maxExit = validExits.reduce((max, date) => (date > max ? date : max), validExits[0]);
  const rangeStart = new Date(minEntry);
  rangeStart.setDate(rangeStart.getDate() - 7);
  const rangeEnd = new Date(maxExit);
  rangeEnd.setDate(rangeEnd.getDate() + 7);

  const days = buildDailyRange(rangeStart, rangeEnd);
  const priceSeries: TradeAnalyzerPoint[] = days.map((date, index) => {
    const wave = Math.sin(index * 0.35) * 0.9 + Math.cos(index * 0.17) * 0.5;
    const trend = avgDelta * index;
    const close = Number((basePrice + trend + wave).toFixed(2));
    return { date, close: Math.max(close, 1) };
  });

  const indexByDate = new Map(priceSeries.map((point, index) => [point.date, index]));

  params.trades.forEach((trade) => {
    const entryIndex = indexByDate.get(trade.entry);
    const exitIndex = indexByDate.get(trade.exit);

    if (entryIndex === undefined || exitIndex === undefined || exitIndex <= entryIndex) {
      return;
    }

    const entryPrice = priceSeries[entryIndex].close;
    const targetExitPrice = Number((entryPrice * (1 + trade.pnl / 100)).toFixed(2));
    const span = exitIndex - entryIndex;

    for (let step = 1; step <= span; step += 1) {
      const ratio = step / span;
      const base = entryPrice + (targetExitPrice - entryPrice) * ratio;
      const noise = Math.sin((entryIndex + step) * 0.5) * 0.18;
      priceSeries[entryIndex + step].close = Number((base + noise).toFixed(2));
    }
  });

  const entryMarkers: TradeMarker[] = [];
  const exitMarkers: TradeMarker[] = [];

  params.trades.forEach((trade) => {
    const entryIndex = indexByDate.get(trade.entry);
    const exitIndex = indexByDate.get(trade.exit);
    if (entryIndex === undefined || exitIndex === undefined) {
      return;
    }

    entryMarkers.push({
      date: trade.entry,
      value: priceSeries[entryIndex].close,
      tradeId: trade.id,
      symbol: trade.symbol,
      side: trade.side,
      action: "entry",
      pnl: trade.pnl,
    });
    exitMarkers.push({
      date: trade.exit,
      value: priceSeries[exitIndex].close,
      tradeId: trade.id,
      symbol: trade.symbol,
      side: trade.side,
      action: "exit",
      pnl: trade.pnl,
    });
  });

  return { priceSeries, entryMarkers, exitMarkers };
}
