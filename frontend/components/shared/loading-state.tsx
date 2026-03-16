"use client";

export function LoadingState({
  label = "Загрузка данных...",
}: {
  label?: string;
}) {
  return (
    <div className="rounded-xl border border-border/75 bg-[linear-gradient(145deg,rgba(55,36,90,0.55),rgba(16,12,30,0.55))] p-3 shadow-[inset_0_1px_0_rgba(255,255,255,0.05)]">
      <div className="mb-2 text-xs text-muted-foreground">{label}</div>
      <div className="space-y-2">
        <div className="h-2 w-full animate-pulse rounded bg-[linear-gradient(90deg,rgba(126,79,224,0.35),rgba(182,133,255,0.35))]" />
        <div className="h-2 w-5/6 animate-pulse rounded bg-[linear-gradient(90deg,rgba(126,79,224,0.35),rgba(182,133,255,0.35))]" />
        <div className="h-2 w-2/3 animate-pulse rounded bg-[linear-gradient(90deg,rgba(126,79,224,0.35),rgba(182,133,255,0.35))]" />
      </div>
    </div>
  );
}
