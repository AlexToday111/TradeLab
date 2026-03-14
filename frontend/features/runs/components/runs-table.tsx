"use client";

import { Run } from "@/lib/types";
import { Checkbox } from "@/components/ui/checkbox";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { RunStatusBadge } from "@/features/runs/components/run-badges";
import { getRunTagLabel } from "@/lib/ui-text";

export function RunsTable({
  runs,
  selectedIds,
  onToggle,
  onRowClick,
}: {
  runs: Run[];
  selectedIds: string[];
  onToggle: (id: string) => void;
  onRowClick: (id: string) => void;
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
          <TableHead>Теги</TableHead>
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
            <TableCell className="flex flex-wrap gap-1">
              {run.tags.map((tag) => (
                <Badge key={tag} variant="secondary" className="text-[10px]">
                  {getRunTagLabel(tag)}
                </Badge>
              ))}
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
