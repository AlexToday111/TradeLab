"use client";

import { EmptyState } from "@/components/layout/empty-state";

export default function ResearchPage() {
  return (
    <EmptyState
      title="Пространство исследований"
      description="Здесь будут жить продвинутые ноутбуки и исследование стратегий."
      actionLabel="Создать ноутбук"
    />
  );
}
