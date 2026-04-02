"use client";

import { createContext, useContext, useEffect, useState, type ReactNode } from "react";

export type InterfaceTheme = "black";

type ThemeOption = {
  value: InterfaceTheme | "white";
  label: string;
  disabled?: boolean;
};

type ThemeContextValue = {
  theme: InterfaceTheme;
  setTheme: (theme: InterfaceTheme) => void;
};

const STORAGE_KEY = "t360lab.interface-theme";

export const interfaceThemeOptions: ThemeOption[] = [
  {
    value: "black",
    label: "Black",
  },
  {
    value: "white",
    label: "White",
    disabled: true,
  },
];

const ThemeContext = createContext<ThemeContextValue | null>(null);

function isInterfaceTheme(value: string | null): value is InterfaceTheme {
  return value === "black";
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setTheme] = useState<InterfaceTheme>("black");

  useEffect(() => {
    const savedTheme = window.localStorage.getItem(STORAGE_KEY);

    if (isInterfaceTheme(savedTheme)) {
      setTheme(savedTheme);
    }
  }, []);

  useEffect(() => {
    document.documentElement.dataset.theme = "neon";
    window.localStorage.setItem(STORAGE_KEY, theme);
  }, [theme]);

  return <ThemeContext.Provider value={{ theme, setTheme }}>{children}</ThemeContext.Provider>;
}

export function useTheme() {
  const context = useContext(ThemeContext);

  if (!context) {
    throw new Error("useTheme must be used within ThemeProvider");
  }

  return context;
}
