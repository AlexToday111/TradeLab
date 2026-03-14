"use client";

import { EmptyState } from "@/components/shared/empty-state";
import { PageHeader } from "@/components/shared/page-header";

export default function DeployPage() {
  return (
    <div className="flex h-full flex-col gap-5">
      <PageHeader
        eyebrow="Деплой"
        title="Деплой"
        description="Раздел для поставки стратегий и рабочих сценариев в дальнейшие окружения."
      />
      <EmptyState
        title="Деплой отключен"
        description="Фича-флаг скрывает инструменты деплоя до включения."
      />
    </div>
  );
}
