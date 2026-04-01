"use client";

import Image from "next/image";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { Github, Settings2 } from "lucide-react";
import { interfaceThemeOptions, type InterfaceTheme, useTheme } from "@/components/theme/theme-provider";
import { navItems } from "@/components/shell/sidebar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { cn } from "@/lib/utils";

export function Topbar() {
  const pathname = usePathname();
  const { theme, setTheme } = useTheme();
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
                    ? "border-[#c7ee51]/40 bg-[#c7ee51]/14 text-[#c7ee51] shadow-[inset_0_1px_0_rgba(255,255,255,0.08)]"
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
                        isActive ? "text-[#c7ee51]" : "text-white/42 group-hover:text-white/70"
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
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button
                type="button"
                className="inline-flex h-10 w-10 items-center justify-center rounded-full border border-transparent text-white/50 transition-all duration-200 hover:border-white/[0.06] hover:bg-white/[0.04] hover:text-white/80"
                aria-label="Настройки интерфейса"
              >
                <Image
                  src="/icons/settings.svg"
                  alt=""
                  width={18}
                  height={18}
                  className="h-[18px] w-[18px] shrink-0"
                  aria-hidden="true"
                />
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent
              align="end"
              sideOffset={10}
              className="w-[320px] border-[rgba(110,226,166,0.16)] bg-[linear-gradient(160deg,rgba(12,18,28,0.96),rgba(8,11,18,0.96))] p-2"
            >
              <DropdownMenuLabel className="px-3 pt-2 text-base text-white/92">
                Настройки
              </DropdownMenuLabel>
              <div className="px-3 pb-2 text-xs text-white/52">
                Пока здесь доступен только выбор темы интерфейса.
              </div>
              <DropdownMenuSeparator className="bg-white/[0.07]" />
              <div className="px-3 pb-2 pt-1 text-[11px] uppercase tracking-[0.22em] text-white/38">
                Тема интерфейса
              </div>
              <DropdownMenuRadioGroup
                value={theme}
                onValueChange={(value) => setTheme(value as InterfaceTheme)}
              >
                {interfaceThemeOptions.map((option) => (
                  <DropdownMenuRadioItem
                    key={option.value}
                    value={option.value}
                    className="cursor-pointer rounded-[12px] py-2.5 pl-8 pr-3 focus:bg-white/[0.05]"
                  >
                    <div className="flex min-w-0 flex-1 items-start justify-between gap-3">
                      <div className="min-w-0">
                        <div className="text-sm font-medium text-white/90">{option.label}</div>
                        <div className="text-[11px] leading-5 text-white/48">
                          {option.description}
                        </div>
                      </div>
                      <span className="shrink-0 text-[10px] uppercase tracking-[0.22em] text-white/34">
                        {option.accentLabel}
                      </span>
                    </div>
                  </DropdownMenuRadioItem>
                ))}
              </DropdownMenuRadioGroup>
            </DropdownMenuContent>
          </DropdownMenu>
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
