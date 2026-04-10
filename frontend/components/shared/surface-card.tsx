"use client";

import { ReactNode } from "react";
import { cn } from "@/lib/utils";

export function SurfaceCard({
  title,
  subtitle,
  actions,
  children,
  overflow = "hidden",
  className,
  contentClassName,
}: {
  title?: string;
  subtitle?: string;
  actions?: ReactNode;
  children: ReactNode;
  overflow?: "hidden" | "visible";
  className?: string;
  contentClassName?: string;
}) {
  return (
    <section
      className={cn(
        "rounded-[28px] border border-[hsl(var(--tl-border-1)/0.72)] bg-[linear-gradient(180deg,hsl(var(--tl-bg-1)/0.96),hsl(var(--tl-bg-2)/0.92))] backdrop-blur-xl shadow-[inset_0_1px_0_hsl(var(--tl-glass-highlight)/0.08),0_24px_56px_rgba(0,0,0,0.14),0_0_0_1px_rgba(43,213,118,0.05)]",
        overflow === "visible" ? "overflow-visible" : "overflow-hidden",
        className
      )}
    >
      {title || subtitle || actions ? (
        <div className="flex flex-wrap items-start justify-between gap-3 border-b border-[hsl(var(--tl-border-1)/0.62)] bg-[linear-gradient(145deg,hsl(var(--primary)/0.1),hsl(var(--accent)/0.06)_48%,hsl(var(--tl-bg-1)/0.92))] px-5 py-4">
          <div className="min-w-0 flex-1">
            {title ? (
              <div className="text-sm font-semibold text-foreground">{title}</div>
            ) : null}
            {subtitle ? (
              <div className="mt-1 text-xs text-muted-foreground">{subtitle}</div>
            ) : null}
          </div>
          {actions ? <div className="shrink-0">{actions}</div> : null}
        </div>
      ) : null}
      <div className={cn("p-5", contentClassName)}>{children}</div>
    </section>
  );
}
