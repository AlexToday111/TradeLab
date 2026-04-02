"use client";

import Link from "next/link";
import { ExternalLink, Trash2 } from "lucide-react";
import { Run } from "@/lib/types";
import { Checkbox } from "@/components/ui/checkbox";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { RunStatusBadge } from "@/features/runs/components/run-badges";

export function RunsTable({
  runs,
  selectedIds,
  onToggle,
  onRowClick,
  onDelete,
}: {
  runs: Run[];
  selectedIds: string[];
  onToggle: (id: string) => void;
  onRowClick: (id: string) => void;
  onDelete: (id: string) => void;
}) {
  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead className="w-10"></TableHead>
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
        {runs.map((run) => (
          <TableRow
            key={run.id}
            className="cursor-pointer hover:bg-panel-subtle"
            onClick={() => onRowClick(run.id)}
          >
            <TableCell onClick={(event) => event.stopPropagation()}>
              <Checkbox
                checked={selectedIds.includes(run.id)}
                onCheckedChange={() => onToggle(run.id)}
              />
            </TableCell>
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
            <TableCell className="w-[108px]" onClick={(event) => event.stopPropagation()}>
              <div className="flex justify-end gap-2">
                <Button
                  type="button"
                  size="icon"
                  variant="secondary"
                  className="h-8 w-8 rounded-full"
                  onClick={() => onDelete(run.id)}
                  title="Удалить"
                  aria-label="Удалить"
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
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
        ))}
      </TableBody>
    </Table>
  );
}
