"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { navItems } from "@/components/shell/sidebar";
import { cn } from "@/lib/utils";

export function Topbar() {
  const pathname = usePathname();
  const visibleNavItems = navItems.filter((item) => !item.gated);

  return (
    <header className="border-b border-border bg-panel/80 px-4 py-2 backdrop-blur">
      <nav className="grid w-full grid-flow-col auto-cols-fr gap-2">
        {visibleNavItems.map((item) => {
          const Icon = item.icon;
          const isActive =
            pathname === item.href ||
            (item.href !== "/workspace" && pathname.startsWith(`${item.href}/`));

          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "flex h-10 items-center justify-center gap-2 rounded-lg border border-transparent px-2 text-sm transition-all duration-200",
                isActive
                  ? "border-white/35 bg-secondary/70 text-foreground shadow-[0_0_10px_rgba(255,255,255,0.22)]"
                  : "text-muted-foreground hover:border-white/80 hover:bg-panel hover:text-foreground hover:shadow-[0_0_14px_rgba(255,255,255,0.6)]"
              )}
            >
              <Icon className="h-4 w-4 shrink-0" />
              <span className="truncate leading-none">{item.label}</span>
            </Link>
          );
        })}
      </nav>
    </header>
  );
}
