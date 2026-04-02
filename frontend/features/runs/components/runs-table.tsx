"use client";

import Link from "next/link";
import { ExternalLink } from "lucide-react";
import { Run } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { RunStatusBadge } from "@/features/runs/components/run-badges";
import { cn } from "@/lib/utils";

export function RunsTable({
  runs,
  selectedIds,
  onToggle,
}: {
  runs: Run[];
  selectedIds: string[];
  onToggle: (id: string) => void;
}) {
  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Статус</TableHead>
          <TableHead>Запуск</TableHead>
          <TableHead>Стратегия</TableHead>
          <TableHead>Датасет</TableHead>
          <TableHead>Период</TableHead>
          <TableHead>PnL</TableHead>
          <TableHead>Шарп</TableHead>
          <TableHead>Макс. просадка</TableHead>
          <TableHead>Сделки</TableHead>
          <TableHead className="text-right">Действия</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {runs.map((run) => {
          const isSelected = selectedIds.includes(run.id);

          return (
            <TableRow
              key={run.id}
              className={cn(
                "cursor-pointer hover:bg-panel-subtle",
                isSelected &&
                  "bg-[linear-gradient(135deg,rgba(43,213,118,0.20),rgba(111,247,163,0.10))]"
              )}
              onClick={() => onToggle(run.id)}
            >
              <TableCell>
                <RunStatusBadge status={run.status} />
              </TableCell>
              <TableCell className="font-mono text-xs text-foreground">
                {run.id}
              </TableCell>
              <TableCell className="text-xs text-muted-foreground">
                {run.strategy}
              </TableCell>
              <TableCell className="text-xs text-muted-foreground">
                {run.datasetVersion}
              </TableCell>
              <TableCell className="text-xs text-muted-foreground">
                {run.period}
              </TableCell>
              <TableCell className="text-xs text-profit">
                {run.metrics.pnl.toFixed(1)}%
              </TableCell>
              <TableCell className="text-xs text-muted-foreground">
                {run.metrics.sharpe.toFixed(2)}
              </TableCell>
              <TableCell className="text-xs text-loss">
                {run.metrics.maxDrawdown.toFixed(1)}%
              </TableCell>
              <TableCell className="text-xs text-muted-foreground">
                {run.metrics.trades}
              </TableCell>
              <TableCell className="w-[56px]" onClick={(event) => event.stopPropagation()}>
                <div className="flex justify-end">
                  <Button
                    asChild
                    size="icon"
                    variant="secondary"
                    className="h-8 w-8 rounded-full"
                  >
                    <Link href={`/runs/${run.id}`} aria-label="Открыть" title="Открыть">
                      <ExternalLink className="h-4 w-4" />
                    </Link>
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          );
        })}
      </TableBody>
    </Table>
  );
}
