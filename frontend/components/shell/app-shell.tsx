"use client";

import { ReactNode } from "react";
import { Topbar } from "@/components/shell/topbar";

export function AppShell({ children }: { children: ReactNode }) {
  return (
    <div className="flex h-screen w-full flex-col overflow-hidden bg-[url('/backgrounds/data.png')] bg-cover bg-center bg-fixed text-foreground">
      <Topbar />
      <main className="min-h-0 flex-1 bg-background/72">
        <div className="h-full overflow-y-auto p-4">{children}</div>
      </main>
    </div>
  );
}
