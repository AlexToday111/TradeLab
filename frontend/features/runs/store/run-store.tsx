"use client";

import { createContext, useContext, useEffect, useState } from "react";
import { Run } from "@/lib/types";
import { initialRuns } from "@/lib/demo-data/runs";

type RunStore = {
  runs: Run[];
  addRun: (run: Run) => void;
  updateRun: (id: string, update: Partial<Run>) => void;
  getRunById: (id: string) => Run | undefined;
};

const RunStoreContext = createContext<RunStore | null>(null);
const STORAGE_KEY = "tradelab:runs";

export function RunStoreProvider({ children }: { children: React.ReactNode }) {
  const [runs, setRuns] = useState<Run[]>(initialRuns);
  const [hydrated, setHydrated] = useState(false);

  useEffect(() => {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
      try {
        setRuns(JSON.parse(stored));
      } catch {
        setRuns(initialRuns);
      }
    }
    setHydrated(true);
  }, []);

  useEffect(() => {
    if (!hydrated) return;
    localStorage.setItem(STORAGE_KEY, JSON.stringify(runs));
  }, [hydrated, runs]);

  const addRun = (run: Run) => {
    setRuns((prev) => [run, ...prev]);
  };

  const updateRun = (id: string, update: Partial<Run>) => {
    setRuns((prev) =>
      prev.map((run) => (run.id === id ? { ...run, ...update } : run))
    );
  };

  const getRunById = (id: string) => runs.find((run) => run.id === id);

  return (
    <RunStoreContext.Provider value={{ runs, addRun, updateRun, getRunById }}>
      {children}
    </RunStoreContext.Provider>
  );
}

export function useRuns() {
  const context = useContext(RunStoreContext);
  if (!context) {
    throw new Error("useRuns must be used within RunStoreProvider");
  }
  return context;
}
