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

export const navItems = [
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
    <aside className="glass-panel flex h-screen w-[220px] shrink-0 flex-col border-r px-3 py-4">
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
                    "group flex items-center gap-3 rounded-xl border border-transparent px-3 py-2 text-sm transition-all duration-300",
                    isActive
                      ? "border-primary/45 bg-[linear-gradient(135deg,rgba(152,102,255,0.32),rgba(76,45,138,0.16))] text-foreground shadow-[0_0_18px_rgba(146,92,255,0.35)]"
                      : "text-muted-foreground hover:border-primary/30 hover:bg-[linear-gradient(135deg,rgba(120,82,210,0.22),rgba(44,28,84,0.1))] hover:text-foreground hover:shadow-[0_0_16px_rgba(136,86,243,0.24)]"
                  )}
                >
                  <Icon className="h-4 w-4 shrink-0 transition-transform duration-300 group-hover:scale-110" />
                  <span className="leading-none">{item.label}</span>
                </Link>
              );
            })}
        </nav>
      </div>
    </aside>
  );
}
