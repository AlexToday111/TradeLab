"use client";

import Image from "next/image";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { Github, Settings2 } from "lucide-react";
import { navItems } from "@/components/shell/sidebar";
import { cn } from "@/lib/utils";

export function Topbar() {
  const pathname = usePathname();
  const primaryNavItems = navItems.filter(
    (item) => !item.gated && item.href !== "/settings"
  );
  const isSettingsActive =
    pathname === "/settings" || pathname.startsWith("/settings/");

  return (
    <header className="relative z-20 border-b border-white/[0.05] bg-[linear-gradient(180deg,rgba(11,13,20,0.98),rgba(9,11,18,0.98))] px-4 py-4 md:px-6">
      <div className="flex items-center gap-5">
        <Link
          href="/workspace"
          className="group flex shrink-0 items-center px-1 py-1 transition-opacity duration-300 hover:opacity-90"
          aria-label="TradeLab home"
        >
          <Image
            src="/Logo.png"
            alt="TradeLab logo"
            width={240}
            height={90}
            className="h-[50px] w-auto origin-left scale-[1.15] object-contain md:h-[55px]"
            priority
          />
        </Link>

        <nav className="mx-auto flex min-w-0 flex-1 items-center justify-start gap-2 overflow-x-auto px-1 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden lg:justify-center">
          {primaryNavItems.map((item) => {
            const Icon = item.icon;
            const isActive =
              pathname === item.href ||
              (item.href !== "/workspace" && pathname.startsWith(`${item.href}/`));

            return (
              <Link
                key={item.href}
                href={item.href}
                className={cn(
                  "group flex h-10 shrink-0 items-center gap-2 rounded-full border px-4 text-[13px] font-medium text-white/66 transition-all duration-200",
                  isActive
                    ? "border-white/[0.06] bg-white/[0.06] text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.08)]"
                    : "border-transparent bg-transparent hover:border-white/[0.04] hover:bg-white/[0.03] hover:text-white/88"
                )}
              >
                {item.iconSrc ? (
                  <Image
                    src={item.iconSrc}
                    alt=""
                    width={16}
                    height={16}
                    className="h-4 w-4 shrink-0"
                    aria-hidden="true"
                  />
                ) : Icon ? (
                  <Icon
                    className={cn(
                      "h-4 w-4 shrink-0",
                      isActive ? "text-white/80" : "text-white/42 group-hover:text-white/70"
                    )}
                  />
                ) : null}
                <span className="leading-none">{item.label}</span>
              </Link>
            );
          })}
        </nav>

        <div className="ml-auto flex shrink-0 items-center gap-2.5">
          <a
            href="https://t.me/trading360l"
            target="_blank"
            rel="noreferrer"
            className="inline-flex h-10 w-10 items-center justify-center rounded-full border border-transparent text-white/50 transition-all duration-200 hover:border-white/[0.06] hover:bg-white/[0.04] hover:text-white/80"
            aria-label="Telegram"
          >
            <Image
              src="/icons/Telegram--Streamline-Core.svg"
              alt=""
              width={22}
              height={22}
              className="h-[22px] w-[22px] shrink-0"
              aria-hidden="true"
            />
          </a>
          <a
            href="https://github.com/AlexToday111/TradeLab.git"
            target="_blank"
            rel="noreferrer"
            className="inline-flex h-10 w-10 items-center justify-center rounded-full border border-transparent text-white/50 transition-all duration-200 hover:border-white/[0.06] hover:bg-white/[0.04] hover:text-white/80"
            aria-label="GitHub"
          >
            <Github className="h-[18px] w-[18px]" />
          </a>
          {isSettingsActive ? (
            <Link
              href="/settings"
              className="inline-flex h-10 items-center rounded-[12px] border border-[rgba(110,226,166,0.16)] bg-[rgba(43,213,118,0.08)] px-4 text-[13px] text-white/86"
            >
              <Settings2 className="mr-2 h-4 w-4" />
              {"\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438"}
            </Link>
          ) : null}
        </div>
      </div>
      <div className="pointer-events-none absolute inset-x-0 bottom-0 h-px bg-[linear-gradient(90deg,transparent,rgba(111,247,163,0.14),rgba(43,213,118,0.18),transparent)]" />
    </header>
  );
}
