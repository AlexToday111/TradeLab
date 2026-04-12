"use client";

import { EmptyState } from "@/components/shared/empty-state";
import { PageHeader } from "@/components/shared/page-header";

export default function SettingsPage() {
  return (
    <div className="flex min-h-full flex-col gap-5">
      <PageHeader
        eyebrow="Настройки"
        title="Настройки"
        description="Системные параметры и конфигурация среды будут собраны здесь."
      />
      <EmptyState
        title="Панель настроек в подготовке"
        description="Следующим шагом сюда можно вынести хранилище, окружения, интеграции и рабочие пресеты."
      />
    </div>
  );
}
