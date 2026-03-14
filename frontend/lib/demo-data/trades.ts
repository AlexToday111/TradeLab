export type TradeRow = {
  id: string;
  symbol: string;
  entry: string;
  exit: string;
  side: "Long" | "Short";
  pnl: number;
  duration: string;
};

export const trades: TradeRow[] = [
  {
    id: "tr_104",
    symbol: "SPY",
    entry: "2024-03-04",
    exit: "2024-03-12",
    side: "Long",
    pnl: 1.42,
    duration: "8d",
  },
  {
    id: "tr_105",
    symbol: "QQQ",
    entry: "2024-03-08",
    exit: "2024-03-11",
    side: "Short",
    pnl: -0.68,
    duration: "3d",
  },
  {
    id: "tr_106",
    symbol: "IWM",
    entry: "2024-03-14",
    exit: "2024-03-28",
    side: "Long",
    pnl: 0.91,
    duration: "14d",
  },
  {
    id: "tr_107",
    symbol: "EFA",
    entry: "2024-04-01",
    exit: "2024-04-10",
    side: "Short",
    pnl: 0.38,
    duration: "9d",
  },
  {
    id: "tr_108",
    symbol: "EEM",
    entry: "2024-04-03",
    exit: "2024-04-17",
    side: "Long",
    pnl: 1.07,
    duration: "14d",
  },
];
