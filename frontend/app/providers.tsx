"use client";

import { ReactNode } from "react";
import { RunStoreProvider } from "@/features/runs/store/run-store";

export function Providers({ children }: { children: ReactNode }) {
  return <RunStoreProvider>{children}</RunStoreProvider>;
}
