"use client";

import { ReactNode } from "react";
import Link from "next/link";
import { Button } from "@/components/ui/button";

export function EmptyState({
  title,
  description,
  actionLabel,
  actionHref,
  icon,
}: {
  title: string;
  description: string;
  actionLabel?: string;
  actionHref?: string;
  icon?: ReactNode;
}) {
  return (
    <div className="flex flex-col items-start gap-3 rounded-[22px] border border-dashed border-border bg-panel-subtle p-6">
      {icon ? <div className="text-muted-foreground">{icon}</div> : null}
      <div>
        <div className="text-sm font-semibold text-foreground">{title}</div>
        <div className="text-xs text-muted-foreground">{description}</div>
      </div>
      {actionLabel ? (
        actionHref ? (
          <Button size="sm" className="mt-1" asChild>
            <Link href={actionHref}>{actionLabel}</Link>
          </Button>
        ) : (
          <Button size="sm" className="mt-1">
            {actionLabel}
          </Button>
        )
      ) : null}
    </div>
  );
}
