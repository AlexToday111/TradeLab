"use client";

import { createContext, useContext, useEffect, useState, type ReactNode } from "react";

export type InterfaceTheme = "neon" | "graphite" | "amber";

type ThemeOption = {
  value: InterfaceTheme;
  label: string;
  description: string;
  accentLabel: string;
};

type ThemeContextValue = {
  theme: InterfaceTheme;
  setTheme: (theme: InterfaceTheme) => void;
};

const STORAGE_KEY = "t360lab.interface-theme";

export const interfaceThemeOptions: ThemeOption[] = [
  {
    value: "neon",
    label: "Neon Grid",
    description: "Базовая зелёная тема интерфейса.",
    accentLabel: "Lime",
  },
  {
    value: "graphite",
    label: "Graphite Blue",
    description: "Более холодная тёмная палитра с синим свечением.",
    accentLabel: "Blue",
  },
  {
    value: "amber",
    label: "Amber Signal",
    description: "Тёплая ночная тема с янтарными акцентами.",
    accentLabel: "Amber",
  },
];

const ThemeContext = createContext<ThemeContextValue | null>(null);

function isInterfaceTheme(value: string | null): value is InterfaceTheme {
  return interfaceThemeOptions.some((option) => option.value === value);
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setTheme] = useState<InterfaceTheme>("neon");

  useEffect(() => {
    const savedTheme = window.localStorage.getItem(STORAGE_KEY);

    if (isInterfaceTheme(savedTheme)) {
      setTheme(savedTheme);
    }
  }, []);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
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
