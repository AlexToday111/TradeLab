"use client";

import { ReactNode } from "react";
import { cn } from "@/lib/utils";

export function PageHeader({
  eyebrow,
  title,
  description,
  actions,
  className,
}: {
  eyebrow?: string;
  title: string;
  description?: string;
  actions?: ReactNode;
  className?: string;
}) {
  return (
    <section
      className={cn(
        "overflow-hidden rounded-[28px] border border-border bg-[linear-gradient(135deg,rgba(92,145,255,0.16),rgba(48,124,108,0.10)_42%,rgba(20,24,35,1)_100%)]",
        className
      )}
    >
      <div className="flex flex-col gap-5 px-6 py-6 lg:flex-row lg:items-end lg:justify-between lg:px-8">
        <div className="max-w-3xl">
          {eyebrow ? (
            <div className="mb-3 text-[11px] uppercase tracking-[0.24em] text-muted-foreground">
              {eyebrow}
            </div>
          ) : null}
          <div className="text-3xl font-semibold tracking-tight text-foreground">
            {title}
          </div>
          {description ? (
            <div className="mt-2 text-sm text-muted-foreground">{description}</div>
          ) : null}
        </div>
        {actions ? <div className="flex flex-wrap gap-2">{actions}</div> : null}
      </div>
    </section>
  );
}
