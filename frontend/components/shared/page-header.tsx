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
        "relative overflow-hidden rounded-[32px] border border-[hsl(var(--tl-border-1)/0.68)] bg-[linear-gradient(135deg,hsl(var(--primary)/0.18),hsl(var(--accent)/0.1)_42%,hsl(var(--tl-bg-1)/0.96)_100%)] shadow-[inset_0_1px_0_hsl(var(--tl-glass-highlight)/0.08),0_22px_48px_rgba(0,0,0,0.14),0_0_0_1px_rgba(43,213,118,0.05)]",
        className
      )}
    >
      <div className="pointer-events-none absolute -left-12 -top-20 h-44 w-44 rounded-full bg-[radial-gradient(circle,rgba(111,247,163,0.24)_0%,rgba(111,247,163,0)_72%)] blur-2xl" />
      <div className="pointer-events-none absolute -right-16 top-0 h-52 w-52 rounded-full bg-[radial-gradient(circle,rgba(43,213,118,0.2)_0%,rgba(43,213,118,0)_72%)] blur-2xl" />
      <div
        className={cn(
          "relative flex flex-col gap-5 px-6 py-6 lg:flex-row lg:items-end lg:px-8",
          hasHeading ? "lg:justify-between" : "justify-end"
        )}
      >
        {hasHeading ? (
          <div className="max-w-3xl">
            {hasEyebrow ? (
              <div className="mb-3 text-[11px] font-medium uppercase tracking-[0.24em] text-accent">
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
        {actions ? <div className="flex flex-wrap items-center gap-2">{actions}</div> : null}
      </div>
    </section>
  );
}
