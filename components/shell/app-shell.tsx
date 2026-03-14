"use client";

import { ReactNode } from "react";
import { Topbar } from "@/components/shell/topbar";

export function AppShell({ children }: { children: ReactNode }) {
  return (
    <div className="flex h-screen w-full flex-col overflow-hidden bg-background text-foreground">
      <Topbar />
      <main className="min-h-0 flex-1 bg-background">
        <div className="h-full overflow-y-auto p-4">{children}</div>
      </main>
    </div>
  );
}
