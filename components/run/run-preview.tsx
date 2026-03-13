"use client";

export function RunPreview() {
  return (
    <div className="rounded-lg border border-border bg-panel-subtle p-3 text-xs">
      <div className="mb-2 text-xs font-semibold text-foreground">
        Предпросмотр запуска
      </div>
      <div className="grid grid-cols-2 gap-2 text-muted-foreground">
        <div>
          <div className="text-[11px] uppercase">Размер датасета</div>
          <div className="text-foreground">14.2 GB</div>
        </div>
        <div>
          <div className="text-[11px] uppercase">Оценка времени</div>
          <div className="text-foreground">1 мин 32 с</div>
        </div>
        <div>
          <div className="text-[11px] uppercase">Предупреждения</div>
          <div className="text-status-warning">2 небольших пропуска</div>
        </div>
        <div>
          <div className="text-[11px] uppercase">Артефакты</div>
          <div className="text-foreground">логи, отчет</div>
        </div>
      </div>
    </div>
  );
}
