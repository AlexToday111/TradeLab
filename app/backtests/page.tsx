"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Filter, Download, Repeat } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useRuns } from "@/components/run/run-store";
import { RunsTable } from "@/components/tables/runs-table";

export default function BacktestsPage() {
  const router = useRouter();
  const { runs } = useRuns();
  const [selected, setSelected] = useState<string[]>([]);

  const toggleRun = (id: string) => {
    setSelected((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center justify-between">
        <div>
          <div className="text-lg font-semibold text-foreground">Backtests</div>
          <div className="text-xs text-muted-foreground">
            CI-style run queue with metrics and tags.
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button size="sm" variant="secondary">
            <Repeat className="mr-2 h-4 w-4" />
            Re-run
          </Button>
          <Button size="sm">
            <Download className="mr-2 h-4 w-4" />
            Bulk export
          </Button>
        </div>
      </div>

      <div className="flex flex-wrap items-center gap-2 rounded-lg border border-border bg-panel p-3">
        <div className="flex items-center gap-2 text-xs text-muted-foreground">
          <Filter className="h-4 w-4" />
          Filters
        </div>
        <Input className="h-8 w-[200px] text-xs" placeholder="Search runs" />
        <Select defaultValue="all">
          <SelectTrigger className="h-8 w-[160px] text-xs">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All statuses</SelectItem>
            <SelectItem value="queued">Queued</SelectItem>
            <SelectItem value="running">Running</SelectItem>
            <SelectItem value="done">Done</SelectItem>
            <SelectItem value="failed">Failed</SelectItem>
          </SelectContent>
        </Select>
        <Select defaultValue="any">
          <SelectTrigger className="h-8 w-[160px] text-xs">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="any">Any tag</SelectItem>
            <SelectItem value="baseline">Baseline</SelectItem>
            <SelectItem value="candidate">Candidate</SelectItem>
            <SelectItem value="prod-like">Prod-like</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="rounded-lg border border-border bg-panel">
        <RunsTable
          runs={runs}
          selectedIds={selected}
          onToggle={toggleRun}
          onRowClick={(id) => router.push(`/runs/${id}`)}
        />
      </div>
    </div>
  );
}
