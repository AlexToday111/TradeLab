"use client";

import { ReactNode } from "react";
import { ClientOnly } from "@/components/layout/client-only";
import { LoadingState } from "@/components/layout/loading-state";

export function ChartCard({
  title,
  subtitle,
  children,
}: {
  title: string;
  subtitle?: string;
  children: ReactNode;
}) {
  return (
    <div className="rounded-lg border border-border bg-panel p-4">
      <div className="mb-3">
        <div className="text-sm font-semibold text-foreground">{title}</div>
        {subtitle ? (
          <div className="text-xs text-muted-foreground">{subtitle}</div>
        ) : null}
      </div>
      <div className="h-48">
        <ClientOnly fallback={<LoadingState label="Отрисовка графика..." />}>
          {children}
        </ClientOnly>
      </div>
    </div>
  );
}
