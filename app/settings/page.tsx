"use client";

import { Card } from "@/components/ui/card";

export default function SettingsPage() {
  return (
    <div className="flex h-full flex-col gap-4">
      <div>
        <div className="text-lg font-semibold text-foreground">Настройки</div>
        <div className="text-xs text-muted-foreground">
          Параметры рабочей области и конфигурация окружения.
        </div>
      </div>
      <Card className="border-border bg-panel p-4 text-xs text-muted-foreground">
        Заглушка панели настроек. Здесь можно добавить API-ключи, хранилище и
        пресеты раскладки.
      </Card>
    </div>
  );
}
