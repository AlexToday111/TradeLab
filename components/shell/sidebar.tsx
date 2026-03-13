"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutGrid,
  BriefcaseBusiness,
  Database,
  Activity,
  Beaker,
  Rocket,
  Settings,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { LogoPlaceholder } from "@/components/shell/logo-placeholder";

const SHOW_DEPLOY = false;

const navItems = [
  { label: "Главное", href: "/workspace", icon: LayoutGrid },
  { label: "Рабочий стол", href: "/desktop", icon: BriefcaseBusiness },
  { label: "Данные", href: "/data", icon: Database },
  { label: "Бэктесты", href: "/backtests", icon: Activity },
  { label: "Исследования", href: "/research", icon: Beaker },
  { label: "Деплой", href: "/deploy", icon: Rocket, gated: true },
  { label: "Настройки", href: "/settings", icon: Settings },
];

export function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="flex h-screen w-[220px] shrink-0 flex-col border-r border-border bg-panel/70 px-3 py-4">
      <div className="flex justify-center pb-4">
        <LogoPlaceholder />
      </div>
      <div className="flex flex-1 items-center">
        <nav className="flex w-full flex-col gap-1">
          {navItems
            .filter((item) => (item.gated ? SHOW_DEPLOY : true))
            .map((item) => {
              const Icon = item.icon;
              const isActive =
                pathname === item.href ||
                (item.href !== "/workspace" && pathname.startsWith(`${item.href}/`));
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={cn(
                    "flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition",
                    isActive
                      ? "bg-secondary/70 text-foreground"
                      : "text-muted-foreground hover:bg-panel hover:text-foreground"
                  )}
                >
                  <Icon className="h-4 w-4 shrink-0" />
                  <span className="leading-none">{item.label}</span>
                </Link>
              );
            })}
        </nav>
      </div>
    </aside>
  );
}
