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
  title?: string;
  description?: string;
  actions?: ReactNode;
  className?: string;
}) {
  const hasEyebrow = Boolean(eyebrow?.trim());
  const hasTitle = Boolean(title?.trim());
  const hasDescription = Boolean(description?.trim());
  const hasHeading = hasEyebrow || hasTitle || hasDescription;

  return (
    <section
      className={cn(
        "overflow-hidden rounded-[28px] border border-border bg-[linear-gradient(135deg,rgba(92,145,255,0.16),rgba(48,124,108,0.10)_42%,rgba(20,24,35,1)_100%)]",
        className
      )}
    >
      <div
        className={cn(
          "flex flex-col gap-5 px-6 py-6 lg:flex-row lg:items-end lg:px-8",
          hasHeading ? "lg:justify-between" : "justify-end"
        )}
      >
        {hasHeading ? (
          <div className="max-w-3xl">
            {hasEyebrow ? (
              <div className="mb-3 text-[11px] uppercase tracking-[0.24em] text-muted-foreground">
                {eyebrow}
              </div>
            ) : null}
            {hasTitle ? (
              <div className="text-3xl font-semibold tracking-tight text-foreground">
                {title}
              </div>
            ) : null}
            {hasDescription ? (
              <div className="mt-2 text-sm text-muted-foreground">{description}</div>
            ) : null}
          </div>
        ) : null}
        {actions ? <div className="flex flex-wrap gap-2">{actions}</div> : null}
      </div>
    </section>
  );
}
