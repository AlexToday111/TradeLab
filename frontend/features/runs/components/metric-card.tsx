"use client";

import { cn } from "@/lib/utils";

export function MetricCard({
  label,
  value,
  hint,
  tone,
}: {
  label: string;
  value: string;
  hint?: string;
  tone?: "profit" | "loss";
}) {
  return (
    <div className="rounded-[22px] border border-border bg-panel px-4 py-4">
      <div className="text-[11px] uppercase tracking-[0.18em] text-muted-foreground">
        {label}
      </div>
      <div
        className={cn(
          "mt-2 text-2xl font-semibold text-foreground",
          tone === "profit" && "text-profit",
          tone === "loss" && "text-loss"
        )}
      >
        {value}
      </div>
      {hint ? (
        <div className="mt-1 text-[11px] text-muted-foreground">{hint}</div>
      ) : null}
    </div>
  );
}
