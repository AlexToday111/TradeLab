"use client";

import { EmptyState } from "@/components/layout/empty-state";

export default function DeployPage() {
  return (
    <EmptyState
      title="Деплой отключен"
      description="Фича-флаг скрывает инструменты деплоя до включения."
    />
  );
}
