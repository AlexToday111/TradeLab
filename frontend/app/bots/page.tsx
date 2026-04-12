"use client";

import { EmptyState } from "@/components/shared/empty-state";
import { PageHeader } from "@/components/shared/page-header";

export default function BotsPage() {
  return (
    <div className="flex min-h-full flex-col gap-5">
      <PageHeader
        eyebrow="Боты"
        title="Боты"
        description="Раздел для управления торговыми ботами и подключенными автоматизациями."
      />
      <EmptyState
        title="Панель ботов в подготовке"
        description="Здесь позже появятся боты, расписания запуска, статусы подключений и управление окружениями."
      />
    </div>
  );
}
