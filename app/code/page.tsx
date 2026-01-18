"use client";

import { useCallback, useEffect, useMemo } from "react";
import { useRouter } from "next/navigation";
import {
  Group as ResizablePanelGroup,
  Panel as ResizablePanel,
  useDefaultLayout,
  usePanelRef,
} from "react-resizable-panels";
import { Search, Play, Beaker, ShieldCheck, Download } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { ResizableHandle } from "@/components/layout/resizable-handle";
import { FileTree } from "@/components/panels/file-tree";
import { fileTree, pinnedFiles } from "@/lib/mock-data/files";
import { consoleLines, logLines } from "@/lib/mock-data/logs";
import { useRuns } from "@/components/run/run-store";
import { createMockRun } from "@/lib/mock-data/factory";
import { DataContractIndicator } from "@/components/run/data-contract";
import { RunPreview } from "@/components/run/run-preview";

const codeLines = [
  "def generate_signals(market, features):",
  "    momentum = features.rolling(20).mean()",
  "    regime = market.volatility(60).zscore()",
  "    if regime > 1.2:",
  "        return signals.neutral()",
  "    score = momentum.rank(axis=1)",
  "    return signals.from_rank(score, top=5, bottom=5)",
  "",
  "def rebalance(portfolio, signals):",
  "    target = portfolio.risk_parity(signals)",
  "    return portfolio.rebalance(target, max_turnover=0.12)",
];

export default function CodePage() {
  const router = useRouter();
  const { addRun, updateRun, runs } = useRuns();
  const leftPanelRef = usePanelRef();
  const bottomPanelRef = usePanelRef();
  const storage =
    typeof window === "undefined"
      ? { getItem: () => null, setItem: () => {} }
      : localStorage;
  const horizontalLayout = useDefaultLayout({
    id: "tradelab-code-horizontal",
    storage,
  });
  const verticalLayout = useDefaultLayout({
    id: "tradelab-code-vertical",
    storage,
  });

  const recentRuns = useMemo(() => runs.slice(0, 3), [runs]);

  const handleBacktest = useCallback(() => {
    const newRun = { ...createMockRun(), status: "running" as const };
    addRun(newRun);
    router.push(`/runs/${newRun.id}`);
    setTimeout(() => {
      updateRun(newRun.id, { status: "done" });
    }, 1500);
  }, [addRun, router, updateRun]);

  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      const isMeta = event.metaKey || event.ctrlKey;
      if (!isMeta) return;

      if (event.key === "Enter") {
        event.preventDefault();
        handleBacktest();
      }

      if (event.key.toLowerCase() === "b") {
        event.preventDefault();
        const panel = leftPanelRef.current;
        if (panel) {
          panel.isCollapsed() ? panel.expand() : panel.collapse();
        }
      }

      if (event.key.toLowerCase() === "j") {
        event.preventDefault();
        const panel = bottomPanelRef.current;
        if (panel) {
          panel.isCollapsed() ? panel.expand() : panel.collapse();
        }
      }
    };

    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [bottomPanelRef, handleBacktest, leftPanelRef]);

  return (
    <div className="h-full">
      <ResizablePanelGroup
        id="tradelab-code-horizontal"
        orientation="horizontal"
        className="h-full rounded-lg border border-border bg-panel"
        defaultLayout={horizontalLayout.defaultLayout}
        onLayoutChanged={horizontalLayout.onLayoutChanged}
      >
        <ResizablePanel
          defaultSize={20}
          minSize={16}
          maxSize={28}
          collapsible
          collapsedSize={3}
          panelRef={leftPanelRef}
        >
          <div className="flex h-full flex-col border-r border-border bg-panel p-3">
            <div className="mb-3 text-sm font-semibold text-foreground">
              Project Explorer
            </div>
            <div className="relative mb-4">
              <Search className="absolute left-2 top-2 h-4 w-4 text-muted-foreground" />
              <Input className="h-8 pl-8 text-xs" placeholder="Search files" />
            </div>
            <ScrollArea className="flex-1">
              <div className="mb-4">
                <div className="mb-2 text-[11px] uppercase tracking-wide text-muted-foreground">
                  Tree
                </div>
                <FileTree nodes={fileTree} />
              </div>
              <div className="mb-4">
                <div className="mb-2 text-[11px] uppercase tracking-wide text-muted-foreground">
                  Pinned files
                </div>
                <div className="flex flex-col gap-1 text-xs text-muted-foreground">
                  {pinnedFiles.map((file) => (
                    <div key={file} className="rounded-md border border-border px-2 py-1 text-foreground">
                      {file}
                    </div>
                  ))}
                </div>
              </div>
              <div>
                <div className="mb-2 text-[11px] uppercase tracking-wide text-muted-foreground">
                  Pinned runs
                </div>
                <div className="flex flex-col gap-2 text-xs">
                  {recentRuns.map((run) => (
                    <div
                      key={run.id}
                      className="rounded-md border border-border bg-panel-subtle p-2"
                    >
                      <div className="font-mono text-foreground">{run.id}</div>
                      <div className="text-muted-foreground">{run.strategy}</div>
                    </div>
                  ))}
                </div>
              </div>
            </ScrollArea>
          </div>
        </ResizablePanel>

        <ResizableHandle />

        <ResizablePanel defaultSize={58} minSize={42}>
          <ResizablePanelGroup
            id="tradelab-code-vertical"
            orientation="vertical"
            className="h-full"
            defaultLayout={verticalLayout.defaultLayout}
            onLayoutChanged={verticalLayout.onLayoutChanged}
          >
            <ResizablePanel defaultSize={68} minSize={40}>
              <div className="flex h-full flex-col border-b border-border bg-panel">
                <div className="flex items-center gap-2 border-b border-border px-3 py-2 text-xs text-muted-foreground">
                  <div className="rounded-md bg-secondary px-2 py-1 text-foreground">
                    atlas_momentum.py
                  </div>
                  <div className="rounded-md px-2 py-1">risk_profiles.yaml</div>
                </div>
                <div className="border-b border-border px-3 py-2 text-xs text-muted-foreground">
                  strategies / atlas_momentum.py
                </div>
                <div className="flex flex-1">
                  <div className="flex-1 overflow-auto p-4 font-mono text-xs text-foreground">
                    {codeLines.map((line, index) => (
                      <div key={index} className="grid grid-cols-[32px_1fr] gap-3">
                        <div className="text-muted-foreground">
                          {(index + 1).toString().padStart(2, "0")}
                        </div>
                        <div>{line}</div>
                      </div>
                    ))}
                  </div>
                  <div className="hidden w-16 border-l border-border bg-panel-subtle md:block">
                    <div className="h-full p-2 text-[10px] text-muted-foreground">
                      {codeLines.map((_, index) => (
                        <div key={index} className="h-2">
                          ..
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
            </ResizablePanel>

            <ResizableHandle className="h-1.5 w-full" />

            <ResizablePanel
              defaultSize={32}
              minSize={20}
              maxSize={45}
              collapsible
              collapsedSize={6}
              panelRef={bottomPanelRef}
            >
              <div className="h-full bg-panel">
                <Tabs defaultValue="console" className="flex h-full flex-col">
                  <TabsList className="h-9 rounded-none border-b border-border bg-panel px-3">
                    <TabsTrigger value="console" className="text-xs">
                      Console
                    </TabsTrigger>
                    <TabsTrigger value="logs" className="text-xs">
                      Logs
                    </TabsTrigger>
                    <TabsTrigger value="problems" className="text-xs">
                      Problems
                    </TabsTrigger>
                    <TabsTrigger value="output" className="text-xs">
                      Backtest Output
                    </TabsTrigger>
                    <TabsTrigger value="artifacts" className="text-xs">
                      Artifacts
                    </TabsTrigger>
                  </TabsList>
                  <TabsContent value="console" className="flex-1 p-3">
                    <ScrollArea className="h-full font-mono text-xs text-muted-foreground">
                      {consoleLines.map((line) => (
                        <div key={line}>{line}</div>
                      ))}
                    </ScrollArea>
                  </TabsContent>
                  <TabsContent value="logs" className="flex-1 p-3">
                    <ScrollArea className="h-full font-mono text-xs text-muted-foreground">
                      {logLines.map((line) => (
                        <div key={line}>{line}</div>
                      ))}
                    </ScrollArea>
                  </TabsContent>
                  <TabsContent value="problems" className="flex-1 p-3 text-xs">
                    <div className="space-y-2 text-muted-foreground">
                      <div>2 warnings / 0 errors</div>
                      <div className="rounded-md border border-border bg-panel-subtle p-2">
                        Missing field: split_adjusted
                      </div>
                      <div className="rounded-md border border-border bg-panel-subtle p-2">
                        Volume spikes above z-score 4.0
                      </div>
                    </div>
                  </TabsContent>
                  <TabsContent value="output" className="flex-1 p-3 text-xs">
                    <div className="grid grid-cols-2 gap-2 text-muted-foreground">
                      <div>PnL: 18.4%</div>
                      <div>Sharpe: 1.62</div>
                      <div>Max DD: -9.7%</div>
                      <div>Trades: 842</div>
                    </div>
                  </TabsContent>
                  <TabsContent value="artifacts" className="flex-1 p-3 text-xs">
                    <div className="space-y-2 text-muted-foreground">
                      <div className="rounded-md border border-border bg-panel-subtle p-2">
                        execution.log
                      </div>
                      <div className="rounded-md border border-border bg-panel-subtle p-2">
                        report.pdf
                      </div>
                      <div className="rounded-md border border-border bg-panel-subtle p-2">
                        export_bundle.zip
                      </div>
                    </div>
                  </TabsContent>
                </Tabs>
              </div>
            </ResizablePanel>
          </ResizablePanelGroup>
        </ResizablePanel>

        <ResizableHandle />

        <ResizablePanel defaultSize={22} minSize={18} maxSize={30}>
          <div className="flex h-full flex-col gap-4 bg-panel p-3">
            <div className="text-sm font-semibold text-foreground">Run / Params</div>

            <div className="space-y-3">
              <div>
                <div className="text-xs text-muted-foreground">Strategy entrypoint</div>
                <Select defaultValue="atlas">
                  <SelectTrigger className="h-8 text-xs">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="atlas">atlas_momentum.py</SelectItem>
                    <SelectItem value="orbit">orbit_reversion.py</SelectItem>
                    <SelectItem value="ridge">ridge_carry.py</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <div className="text-xs text-muted-foreground">Dataset selector</div>
                <Select defaultValue="equities">
                  <SelectTrigger className="h-8 text-xs">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="equities">Equities US v13</SelectItem>
                    <SelectItem value="etfs">ETFs Intraday v21</SelectItem>
                    <SelectItem value="fx">FX Daily v09</SelectItem>
                  </SelectContent>
                </Select>
                <div className="mt-2 grid grid-cols-2 gap-2">
                  <Input className="h-8 text-xs" defaultValue="1D" />
                  <Input className="h-8 text-xs" defaultValue="2018 -> 2024" />
                </div>
              </div>
              <div>
                <div className="text-xs text-muted-foreground">Fees / Slippage preset</div>
                <Select defaultValue="base">
                  <SelectTrigger className="h-8 text-xs">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="base">Base (0.8 / 0.5 bps)</SelectItem>
                    <SelectItem value="low">Low (0.4 / 0.3 bps)</SelectItem>
                    <SelectItem value="high">High (1.2 / 0.9 bps)</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <div className="text-xs text-muted-foreground">Execution model</div>
                <Select defaultValue="vwap">
                  <SelectTrigger className="h-8 text-xs">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="vwap">VWAP</SelectItem>
                    <SelectItem value="twap">TWAP</SelectItem>
                    <SelectItem value="market">Market</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <div className="text-xs text-muted-foreground">Risk model</div>
                <div className="grid grid-cols-2 gap-2">
                  <Input className="h-8 text-xs" defaultValue="0.5%/trade" />
                  <Input className="h-8 text-xs" defaultValue="25% max" />
                </div>
              </div>
              <DataContractIndicator />
              <RunPreview />
            </div>

            <div className="mt-auto grid grid-cols-2 gap-2">
              <Button size="sm" onClick={handleBacktest}>
                <Play className="mr-2 h-4 w-4" />
                Backtest
              </Button>
              <Button size="sm" variant="secondary">
                <Beaker className="mr-2 h-4 w-4" />
                Optimize
              </Button>
              <Button size="sm" variant="secondary">
                <ShieldCheck className="mr-2 h-4 w-4" />
                Validate
              </Button>
              <Button size="sm" variant="secondary">
                <Download className="mr-2 h-4 w-4" />
                Export
              </Button>
            </div>

            <div className="rounded-lg border border-border bg-panel-subtle p-2 text-xs text-muted-foreground">
              Hotkeys: Cmd/Ctrl+Enter (run), Cmd/Ctrl+P (search), Cmd/Ctrl+J
              (bottom panel)
            </div>
          </div>
        </ResizablePanel>
      </ResizablePanelGroup>
    </div>
  );
}
