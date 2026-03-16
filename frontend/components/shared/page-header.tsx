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
        "relative overflow-hidden rounded-[28px] border border-border/70 bg-[linear-gradient(135deg,rgba(150,96,255,0.24),rgba(108,62,201,0.14)_42%,rgba(13,9,24,0.96)_100%)] shadow-[inset_0_1px_0_rgba(255,255,255,0.08),0_16px_38px_rgba(0,0,0,0.44)]",
        className
      )}
    >
      <div className="pointer-events-none absolute -left-12 -top-20 h-44 w-44 rounded-full bg-[radial-gradient(circle,rgba(204,174,255,0.4)_0%,rgba(204,174,255,0)_72%)] blur-2xl" />
      <div className="pointer-events-none absolute -right-16 top-0 h-52 w-52 rounded-full bg-[radial-gradient(circle,rgba(146,90,255,0.34)_0%,rgba(146,90,255,0)_72%)] blur-2xl" />
      <div
        className={cn(
          "relative flex flex-col gap-5 px-6 py-6 lg:flex-row lg:items-end lg:px-8",
          hasHeading ? "lg:justify-between" : "justify-end"
        )}
      >
        {hasHeading ? (
          <div className="max-w-3xl">
            {hasEyebrow ? (
              <div className="mb-3 text-[11px] uppercase tracking-[0.24em] text-accent">
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
