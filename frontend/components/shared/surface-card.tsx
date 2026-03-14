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
        "rounded-[24px] border border-border bg-panel shadow-[inset_0_1px_0_rgba(255,255,255,0.02),0_18px_40px_rgba(0,0,0,0.12)]",
        overflow === "visible" ? "overflow-visible" : "overflow-hidden",
        className
      )}
    >
      {title || subtitle || actions ? (
        <div className="flex flex-wrap items-start justify-between gap-3 border-b border-border/80 px-5 py-4">
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
