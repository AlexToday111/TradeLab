"use client";

import { ReactNode } from "react";
import { ClientOnly } from "@/components/shared/client-only";
import { LoadingState } from "@/components/shared/loading-state";
import { SurfaceCard } from "@/components/shared/surface-card";
import { cn } from "@/lib/utils";

export function ChartCard({
  title,
  subtitle,
  actions,
  children,
  chartClassName,
}: {
  title: string;
  subtitle?: string;
  actions?: ReactNode;
  children: ReactNode;
  chartClassName?: string;
}) {
  return (
    <SurfaceCard title={title} subtitle={subtitle} actions={actions} contentClassName="p-5">
      <div className={cn("h-64 xl:h-72", chartClassName)}>
        <ClientOnly fallback={<LoadingState label="Отрисовка графика..." />}>
          {children}
        </ClientOnly>
      </div>
    </SurfaceCard>
  );
}
