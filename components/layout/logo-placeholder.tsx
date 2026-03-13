"use client";

import Link from "next/link";

export function LogoPlaceholder() {
  return (
    <Link
      href="/workspace"
      className="flex items-center gap-3 rounded-xl border border-dashed border-border bg-panel-subtle px-3 py-3 transition hover:border-primary/40 hover:bg-panel"
      aria-label="Открыть рабочую область"
    >
      <div className="flex h-10 w-10 items-center justify-center rounded-lg border border-border bg-background text-[11px] font-medium text-muted-foreground">
        LOGO
      </div>
      <div className="min-w-0">
        <div className="text-sm font-semibold tracking-tight text-foreground">
          TradeLab
        </div>
        <div className="text-[11px] text-muted-foreground">
          Место под логотип
        </div>
      </div>
    </Link>
  );
}
