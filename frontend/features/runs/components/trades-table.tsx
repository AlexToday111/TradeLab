"use client";

import { TradeRow } from "@/lib/demo-data/trades";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { getTradeSideLabel } from "@/lib/ui-text";

export function TradesTable({ rows }: { rows: TradeRow[] }) {
  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Сделка</TableHead>
          <TableHead>Символ</TableHead>
          <TableHead>Направление</TableHead>
          <TableHead>Вход</TableHead>
          <TableHead>Выход</TableHead>
          <TableHead>PnL</TableHead>
          <TableHead>Длительность</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {rows.map((trade) => (
          <TableRow
            key={trade.id}
            className={
              trade.pnl >= 0
                ? "!border-l-4 !border-l-emerald-400/80 !bg-emerald-500/16 hover:!bg-emerald-500/24"
                : "!border-l-4 !border-l-red-400/80 !bg-red-500/16 hover:!bg-red-500/24"
            }
          >
            <TableCell className="font-mono text-xs text-foreground">
              {trade.id}
            </TableCell>
            <TableCell className="text-xs text-muted-foreground">
              {trade.symbol}
            </TableCell>
            <TableCell className="text-xs text-muted-foreground">
              {getTradeSideLabel(trade.side)}
            </TableCell>
            <TableCell className="text-xs text-muted-foreground">
              {trade.entry}
            </TableCell>
            <TableCell className="text-xs text-muted-foreground">
              {trade.exit}
            </TableCell>
            <TableCell className={trade.pnl >= 0 ? "text-xs text-profit" : "text-xs text-loss"}>
              {trade.pnl.toFixed(2)}%
            </TableCell>
            <TableCell className="text-xs text-muted-foreground">
              {trade.duration}
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
