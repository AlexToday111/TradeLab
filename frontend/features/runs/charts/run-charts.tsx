"use client";

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
} from "recharts";
import {
  equityCurve,
  drawdownSeries,
  underwaterSeries,
  returnsHistogram,
  ohlcPreview,
} from "@/lib/demo-data/charts";

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
