"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { navItems } from "@/components/shell/sidebar";
import { cn } from "@/lib/utils";

export function Topbar() {
  const pathname = usePathname();
  const visibleNavItems = navItems.filter((item) => !item.gated);

  return (
    <header className="border-b border-border/70 bg-[linear-gradient(180deg,rgba(35,22,58,0.78),rgba(17,12,30,0.78))] px-4 py-2 backdrop-blur-xl shadow-[0_14px_30px_rgba(0,0,0,0.38)]">
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
                "group flex h-10 items-center justify-center gap-2 rounded-xl border border-transparent px-2 text-sm font-medium tracking-[0.01em] transition-all duration-300",
                isActive
                  ? "border-primary/55 bg-[linear-gradient(135deg,rgba(148,93,255,0.36),rgba(95,51,171,0.16))] text-foreground shadow-[inset_0_1px_0_rgba(255,255,255,0.12),0_0_0_1px_rgba(174,111,255,0.18),0_0_26px_rgba(157,91,255,0.42)]"
                  : "text-muted-foreground hover:border-primary/45 hover:bg-[linear-gradient(135deg,rgba(133,84,235,0.24),rgba(54,34,101,0.1))] hover:text-foreground hover:shadow-[0_0_20px_rgba(148,89,255,0.28)]"
              )}
            >
              <Icon className="h-4 w-4 shrink-0 transition-transform duration-300 group-hover:scale-110" />
              <span className="truncate leading-none">{item.label}</span>
            </Link>
          );
        })}
      </nav>
    </header>
  );
}
