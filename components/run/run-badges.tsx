"use client";

import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import { RunStatus } from "@/lib/types";
import { getRunDiffLabel, getRunStatusLabel } from "@/lib/ui-text";

const statusStyles: Record<RunStatus, string> = {
  queued: "bg-secondary text-muted-foreground border-border",
  running: "bg-status-running/20 text-status-running border-status-running/40",
  done: "bg-status-success/20 text-status-success border-status-success/40",
  failed: "bg-status-error/20 text-status-error border-status-error/40",
};

export function RunStatusBadge({ status }: { status: RunStatus }) {
  return (
    <Badge
      className={cn("border px-2 py-0.5 text-xs font-medium", statusStyles[status])}
      variant="outline"
    >
      {getRunStatusLabel(status)}
    </Badge>
  );
}

export function RunDiffIndicators({
  diff,
}: {
  diff: { code: boolean; data: boolean; config: boolean };
}) {
  const items = [
    { key: "code" as const, active: diff.code },
    { key: "data" as const, active: diff.data },
    { key: "config" as const, active: diff.config },
  ];

  return (
    <div className="flex items-center gap-2 text-xs text-muted-foreground">
      {items.map((item) => (
        <div
          key={item.key}
          className={cn(
            "flex items-center gap-1 rounded-full border px-2 py-0.5",
            item.active
              ? "border-primary/60 text-primary"
              : "border-border text-muted-foreground"
          )}
          >
            <span
              className={cn(
                "h-1.5 w-1.5 rounded-full",
                item.active ? "bg-primary" : "bg-muted-foreground"
              )}
            />
          {getRunDiffLabel(item.key)}
        </div>
      ))}
    </div>
  );
}
