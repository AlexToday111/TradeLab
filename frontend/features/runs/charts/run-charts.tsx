"use client";

import { useMemo } from "react";
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  LineChart,
  Line,
  BarChart,
  Bar,
  CartesianGrid,
  ComposedChart,
  Scatter,
  Legend,
  Brush,
} from "recharts";
import {
  equityCurve,
  drawdownSeries,
  underwaterSeries,
  returnsHistogram,
  ohlcPreview,
  buildTradeAnalyzerFromDataset,
  type DatasetPriceRow,
} from "@/lib/demo-data/charts";
import type { TradeRow } from "@/lib/demo-data/trades";

const axisProps = {
  tick: { fill: "hsl(var(--muted-foreground))", fontSize: 11 },
  axisLine: { stroke: "hsl(var(--border))" },
  tickLine: { stroke: "hsl(var(--border))" },
};

const tooltipStyle = {
  background: "hsl(var(--popover))",
  border: "1px solid hsl(var(--border))",
  color: "hsl(var(--foreground))",
  fontSize: "12px",
};

const analyzerTooltipFormatter = (
  value: number | string | undefined,
  name: string | undefined,
  payload: any
) => {
  const safeValue = value ?? "-";
  const action = String(payload?.action ?? "");
  if (action === "entry") {
    return [`${safeValue}`, "Вход"];
  }
  if (action === "exit") {
    return [`${safeValue}`, "Выход"];
  }
  const safeName = name ?? "Значение";
  return [`${safeValue}`, safeName === "close" ? "Цена" : safeName];
};

export function EquityChart() {
  return (
    <ResponsiveContainer width="100%" height="100%">
      <AreaChart data={equityCurve}>
        <defs>
          <linearGradient id="equityFill" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="hsl(var(--chart-1))" stopOpacity={0.6} />
            <stop offset="100%" stopColor="hsl(var(--chart-1))" stopOpacity={0.05} />
          </linearGradient>
        </defs>
        <CartesianGrid stroke="hsl(var(--border))" strokeDasharray="4 4" />
        <XAxis dataKey="date" {...axisProps} />
        <YAxis {...axisProps} />
        <Tooltip contentStyle={tooltipStyle} />
        <Area
          type="monotone"
          dataKey="value"
          stroke="hsl(var(--chart-1))"
          fill="url(#equityFill)"
          strokeWidth={2}
        />
      </AreaChart>
    </ResponsiveContainer>
  );
}

export function DrawdownChart() {
  return (
    <ResponsiveContainer width="100%" height="100%">
      <LineChart data={drawdownSeries}>
        <CartesianGrid stroke="hsl(var(--border))" strokeDasharray="4 4" />
        <XAxis dataKey="date" {...axisProps} />
        <YAxis {...axisProps} />
        <Tooltip contentStyle={tooltipStyle} />
        <Line
          type="monotone"
          dataKey="value"
          stroke="hsl(var(--chart-4))"
          strokeWidth={2}
          dot={false}
        />
      </LineChart>
    </ResponsiveContainer>
  );
}

export function UnderwaterChart() {
  return (
    <ResponsiveContainer width="100%" height="100%">
      <AreaChart data={underwaterSeries}>
        <defs>
          <linearGradient id="underwaterFill" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="hsl(var(--chart-4))" stopOpacity={0.5} />
            <stop offset="100%" stopColor="hsl(var(--chart-4))" stopOpacity={0.05} />
          </linearGradient>
        </defs>
        <CartesianGrid stroke="hsl(var(--border))" strokeDasharray="4 4" />
        <XAxis dataKey="date" {...axisProps} />
        <YAxis {...axisProps} />
        <Tooltip contentStyle={tooltipStyle} />
        <Area
          type="monotone"
          dataKey="value"
          stroke="hsl(var(--chart-4))"
          fill="url(#underwaterFill)"
          strokeWidth={2}
        />
      </AreaChart>
    </ResponsiveContainer>
  );
}

export function ReturnsHistogramChart() {
  return (
    <ResponsiveContainer width="100%" height="100%">
      <BarChart data={returnsHistogram}>
        <CartesianGrid stroke="hsl(var(--border))" strokeDasharray="4 4" />
        <XAxis dataKey="bucket" {...axisProps} />
        <YAxis {...axisProps} />
        <Tooltip contentStyle={tooltipStyle} />
        <Bar dataKey="value" fill="hsl(var(--chart-2))" radius={[4, 4, 0, 0]} />
      </BarChart>
    </ResponsiveContainer>
  );
}

export function OhlcPreviewChart() {
  return (
    <ResponsiveContainer width="100%" height="100%">
      <LineChart data={ohlcPreview}>
        <XAxis dataKey="date" {...axisProps} />
        <YAxis {...axisProps} />
        <Tooltip contentStyle={tooltipStyle} />
        <Line
          type="monotone"
          dataKey="value"
          stroke="hsl(var(--chart-2))"
          strokeWidth={2}
          dot={false}
        />
      </LineChart>
    </ResponsiveContainer>
  );
}

export function TradesAnalyzerChart({
  datasetRows,
  trades,
}: {
  datasetRows: DatasetPriceRow[];
  trades: TradeRow[];
}) {
  const analyzer = useMemo(
    () => buildTradeAnalyzerFromDataset({ datasetRows, trades }),
    [datasetRows, trades]
  );

  return (
    <ResponsiveContainer width="100%" height="100%">
      <ComposedChart data={analyzer.priceSeries}>
        <CartesianGrid stroke="hsl(var(--border))" strokeDasharray="4 4" />
        <XAxis dataKey="date" minTickGap={36} {...axisProps} />
        <YAxis width={52} {...axisProps} />
        <Tooltip contentStyle={tooltipStyle} formatter={analyzerTooltipFormatter} />
        <Legend />
        <Line
          type="monotone"
          dataKey="close"
          name="Цена"
          stroke="hsl(var(--chart-2))"
          strokeWidth={2.5}
          dot={false}
        />
        <Scatter
          name="Входы"
          data={analyzer.entryMarkers}
          dataKey="value"
          fill="hsl(var(--tl-success))"
        />
        <Scatter
          name="Выходы"
          data={analyzer.exitMarkers}
          dataKey="value"
          fill="hsl(var(--tl-error))"
        />
        <Brush
          dataKey="date"
          height={20}
          stroke="hsl(var(--chart-2))"
          travellerWidth={10}
        />
      </ComposedChart>
    </ResponsiveContainer>
  );
}
