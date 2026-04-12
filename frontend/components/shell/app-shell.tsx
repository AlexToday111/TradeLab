"use client";

import { ReactNode } from "react";
import { Topbar } from "@/components/shell/topbar";

export function AppShell({ children }: { children: ReactNode }) {
  return (
    <div className="relative isolate h-screen min-h-screen w-full overflow-hidden text-foreground">
      <div className="pointer-events-none absolute inset-0 -z-10 overflow-hidden">
        <div className="absolute left-[-10%] top-[-12%] h-[480px] w-[480px] rounded-full bg-[radial-gradient(circle,rgba(43,213,118,0.22)_0%,rgba(43,213,118,0)_70%)] blur-3xl" />
        <div className="absolute right-[-12%] top-[-14%] h-[520px] w-[520px] rounded-full bg-[radial-gradient(circle,rgba(111,247,163,0.18)_0%,rgba(111,247,163,0)_72%)] blur-3xl" />
        <div className="absolute bottom-[-24%] left-[14%] h-[420px] w-[420px] rounded-full bg-[radial-gradient(circle,rgba(43,213,118,0.12)_0%,rgba(43,213,118,0)_74%)] blur-3xl" />
        <div className="absolute bottom-[10%] right-[8%] h-[220px] w-[220px] rounded-full bg-[radial-gradient(circle,rgba(247,147,26,0.08)_0%,rgba(247,147,26,0)_72%)] blur-3xl" />
      </div>
      <div className="mx-auto flex h-full w-full max-w-[1640px] flex-col px-2 py-3 md:px-4 md:py-4">
        <div className="relative flex h-full min-h-0 flex-1 flex-col overflow-hidden rounded-[30px] border border-[hsl(var(--tl-border-1)/0.72)] bg-[linear-gradient(180deg,hsl(var(--tl-bg-1)/0.96),hsl(var(--tl-bg-0)/0.98))] shadow-[inset_0_1px_0_hsl(var(--tl-glass-highlight)/0.08),0_30px_90px_rgba(0,0,0,0.18)]">
          <div className="pointer-events-none absolute inset-0 rounded-[30px] ring-1 ring-inset ring-[hsl(var(--tl-glass-highlight)/0.14)]" />
          <div className="pointer-events-none absolute inset-x-0 top-0 h-24 bg-[linear-gradient(180deg,hsl(var(--tl-glass-highlight)/0.08),transparent)]" />
          <Topbar />
          <main className="relative flex min-h-0 flex-1 overflow-hidden bg-[linear-gradient(180deg,hsl(var(--tl-bg-1)/0.94)_0%,hsl(var(--tl-bg-0)/0.96)_100%)]">
            <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_50%_0%,rgba(43,213,118,0.14),transparent_42%)]" />
            <div className="pointer-events-none absolute inset-0 bg-[linear-gradient(to_right,rgba(92,240,158,0.035)_1px,transparent_1px),linear-gradient(to_bottom,rgba(92,240,158,0.02)_1px,transparent_1px)] bg-[size:80px_80px] opacity-40" />
            <div className="relative min-h-0 flex-1 overflow-y-auto overscroll-contain p-4 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden md:p-5 lg:p-6">
              {children}
            </div>
          </main>
        </div>
      </div>
    </div>
  );
}
