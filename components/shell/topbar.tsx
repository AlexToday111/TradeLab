"use client";

import { Badge } from "@/components/ui/badge";

export function Topbar() {
  return (
    <header className="flex h-14 items-center justify-end border-b border-border bg-panel/80 px-4 backdrop-blur">
      <div className="flex items-center gap-3">
        <Badge className="border border-status-running/40 bg-status-running/20 text-status-running">
          локально
        </Badge>
      </div>
    </header>
  );
}
