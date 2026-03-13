"use client";

export function LoadingState({
  label = "Загрузка данных...",
}: {
  label?: string;
}) {
  return (
    <div className="rounded-lg border border-border bg-panel-subtle p-3">
      <div className="mb-2 text-xs text-muted-foreground">{label}</div>
      <div className="space-y-2">
        <div className="h-2 w-full animate-pulse rounded bg-panel-strong" />
        <div className="h-2 w-5/6 animate-pulse rounded bg-panel-strong" />
        <div className="h-2 w-2/3 animate-pulse rounded bg-panel-strong" />
      </div>
    </div>
  );
}
