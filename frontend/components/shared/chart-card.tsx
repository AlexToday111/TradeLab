"use client";

import { ReactNode } from "react";
import { ClientOnly } from "@/components/shared/client-only";
import { LoadingState } from "@/components/shared/loading-state";
import { SurfaceCard } from "@/components/shared/surface-card";

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
    <SurfaceCard title={title} subtitle={subtitle} contentClassName="p-5">
      <div className="h-52">
        <ClientOnly fallback={<LoadingState label="Отрисовка графика..." />}>
          {children}
        </ClientOnly>
      </div>
    </SurfaceCard>
  );
}
