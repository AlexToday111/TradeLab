"use client";

import { ReactNode } from "react";
import { Topbar } from "@/components/shell/topbar";

export function AppShell({ children }: { children: ReactNode }) {
  return (
    <div className="relative isolate flex h-screen w-full flex-col overflow-hidden text-foreground">
      <div className="pointer-events-none absolute inset-0 -z-10 overflow-hidden">
        <div className="absolute -left-28 -top-24 h-[380px] w-[380px] rounded-full bg-[radial-gradient(circle,rgba(162,92,255,0.45)_0%,rgba(162,92,255,0)_70%)] blur-3xl" />
        <div className="absolute right-[-140px] top-[-120px] h-[420px] w-[420px] rounded-full bg-[radial-gradient(circle,rgba(129,77,255,0.34)_0%,rgba(129,77,255,0)_72%)] blur-3xl" />
        <div className="absolute bottom-[-180px] left-[18%] h-[420px] w-[420px] rounded-full bg-[radial-gradient(circle,rgba(205,170,255,0.2)_0%,rgba(205,170,255,0)_74%)] blur-3xl" />
      </div>
      <Topbar />
      <main className="relative min-h-0 flex-1 bg-[linear-gradient(180deg,rgba(12,9,23,0.52)_0%,rgba(7,5,14,0.88)_100%)]">
        <div className="h-full overflow-y-auto p-4">{children}</div>
      </main>
    </div>
  );
}
