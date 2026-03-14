export type Project = {
  id: string;
  name: string;
  description: string;
  lastDataset: string;
  lastRunId: string;
  lastActive: string;
  recentRuns: string[];
};

export const projects: Project[] = [
  {
    id: "proj-atlas",
    name: "Atlas Momentum",
    description: "Мультиактивный моментум с фильтрами режима.",
    lastDataset: "Акции США v13",
    lastRunId: "run_8f2c",
    lastActive: "2025-01-17 18:42",
    recentRuns: ["run_8f2c", "run_7a21", "run_6b90"],
  },
  {
    id: "proj-orbit",
    name: "Orbit Mean Reversion",
    description: "Внутридневной возврат к среднему на ликвидных ETF.",
    lastDataset: "ETF внутри дня v21",
    lastRunId: "run_51aa",
    lastActive: "2025-01-17 11:12",
    recentRuns: ["run_51aa", "run_4cf0"],
  },
  {
    id: "proj-ridge",
    name: "Ridge Carry",
    description: "Carry-корзина с балансировкой по риску.",
    lastDataset: "FX дневной v09",
    lastRunId: "run_3bce",
    lastActive: "2025-01-16 09:05",
    recentRuns: ["run_3bce", "run_2b11"],
  },
];
