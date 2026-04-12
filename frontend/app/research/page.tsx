"use client";

import { EmptyState } from "@/components/shared/empty-state";
import { PageHeader } from "@/components/shared/page-header";

export default function ResearchPage() {
  return (
    <div className="flex min-h-full flex-col gap-5">
      <PageHeader
        eyebrow="Исследования"
        title="Исследования"
        description="Зона для экспериментов, заметок, гипотез и аналитических сценариев."
      />
      <EmptyState
        title="Пространство исследований"
        description="Здесь будут жить продвинутые ноутбуки и исследование стратегий."
        actionLabel="Создать сценарий"
      />
    </div>
  );
}
