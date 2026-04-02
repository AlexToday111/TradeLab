"use client";

import { ReactNode } from "react";
import { ThemeProvider } from "@/components/theme/theme-provider";
import { RunStoreProvider } from "@/features/runs/store/run-store";

export function Providers({ children }: { children: ReactNode }) {
  return (
    <ThemeProvider>
      <RunStoreProvider>{children}</RunStoreProvider>
    </ThemeProvider>
  );
}
