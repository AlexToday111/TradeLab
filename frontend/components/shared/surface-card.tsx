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
        "rounded-[24px] border border-border/75 bg-[linear-gradient(155deg,rgba(43,28,70,0.72),rgba(14,11,27,0.72))] backdrop-blur-xl shadow-[inset_0_1px_0_rgba(255,255,255,0.08),0_20px_44px_rgba(0,0,0,0.44)]",
        overflow === "visible" ? "overflow-visible" : "overflow-hidden",
        className
      )}
    >
      {title || subtitle || actions ? (
        <div className="flex flex-wrap items-start justify-between gap-3 border-b border-border/65 bg-[linear-gradient(145deg,rgba(96,60,164,0.22),rgba(28,20,52,0.08))] px-5 py-4">
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
