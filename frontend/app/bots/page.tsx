"use client";

import Image from "next/image";
import Link from "next/link";
import { startTransition, useEffect, useState } from "react";
import {
  ArrowUpRight,
  Bell,
  Bot,
  CheckCircle2,
  Command,
  Copy,
  ShieldCheck,
  Sparkles,
  Trash2,
  Workflow,
} from "lucide-react";
import { PageHeader } from "@/components/shared/page-header";
import { SurfaceCard } from "@/components/shared/surface-card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useAuth } from "@/features/auth/auth-provider";
import { apiFetch } from "@/lib/api/client";
import { cn } from "@/lib/utils";

const BOT_STORAGE_KEY = "tradelab.custom-bots";

const botTemplates = [
  {
    id: "monitoring",
    title: "Операционный бот",
    description: "Следит за прогонами, показывает статусы и быстро пересобирается из шаблона.",
    defaultName: "Run Watcher",
    defaultChannel: "telegram",
    defaultMarket: "multi",
    icon: Bell,
  },
  {
    id: "signal",
    title: "Сигнальный бот",
    description: "Собирает сигналы по стратегии и отправляет их в нужный канал без лишней ручной сборки.",
    defaultName: "Signal Desk",
    defaultChannel: "webhook",
    defaultMarket: "crypto",
    icon: Sparkles,
  },
  {
    id: "assistant",
    title: "Исследовательский бот",
    description: "Запускает сценарии для проверки гипотез и держит под рукой связку стратегия + датасет.",
    defaultName: "Research Pilot",
    defaultChannel: "dashboard",
    defaultMarket: "stocks",
    icon: Workflow,
  },
] as const;

const channelOptions = [
  { value: "telegram", label: "Telegram" },
  { value: "dashboard", label: "Dashboard" },
  { value: "webhook", label: "Webhook" },
] as const;

const marketOptions = [
  { value: "multi", label: "Мульти-рынок" },
  { value: "crypto", label: "Крипта" },
  { value: "stocks", label: "Акции" },
  { value: "fx", label: "FX" },
] as const;

type BotTemplateId = (typeof botTemplates)[number]["id"];
type BotChannel = (typeof channelOptions)[number]["value"];
type BotMarket = (typeof marketOptions)[number]["value"];
type BotOrigin = "template" | "company";

type OfficialBotStatus = {
  botName: string;
  botUserName: string;
  codeAvailable: boolean;
  enabled: boolean;
  tokenConfigured: boolean;
  defaultChatConfigured: boolean;
  botStartupEnabled: boolean;
  notificationsEnabled: boolean;
  commands: string[];
};

type CustomBotDraft = {
  name: string;
  templateId: BotTemplateId;
  channel: BotChannel;
  market: BotMarket;
};

type CustomBotRecord = {
  id: string;
  name: string;
  templateId: BotTemplateId;
  channel: BotChannel;
  market: BotMarket;
  origin: BotOrigin;
  summary: string;
  createdAt: string;
};

const defaultTemplate = botTemplates[0];

function getTemplate(templateId: BotTemplateId) {
  return botTemplates.find((template) => template.id === templateId) ?? defaultTemplate;
}

function getChannelLabel(channel: BotChannel) {
  return channelOptions.find((item) => item.value === channel)?.label ?? channel;
}

function getMarketLabel(market: BotMarket) {
  return marketOptions.find((item) => item.value === market)?.label ?? market;
}

function formatCreatedAt(value: string) {
  return new Intl.DateTimeFormat("ru-RU", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value));
}

export default function BotsPage() {
  const { session } = useAuth();
  const storageKey = session ? `${BOT_STORAGE_KEY}:${session.user.id}` : BOT_STORAGE_KEY;
  const [draft, setDraft] = useState<CustomBotDraft>({
    name: defaultTemplate.defaultName,
    templateId: defaultTemplate.id,
    channel: defaultTemplate.defaultChannel,
    market: defaultTemplate.defaultMarket,
  });
  const [draftOrigin, setDraftOrigin] = useState<BotOrigin>("template");
  const [customBots, setCustomBots] = useState<CustomBotRecord[]>([]);
  const [officialBot, setOfficialBot] = useState<OfficialBotStatus | null>(null);
  const [officialBotError, setOfficialBotError] = useState<string>("");

  const selectedTemplate = getTemplate(draft.templateId);
  const companyBotReady = officialBot?.notificationsEnabled ?? false;
  const companyBotBootable = officialBot?.botStartupEnabled ?? false;

  useEffect(() => {
    try {
      const rawBots = window.localStorage.getItem(storageKey);
      if (!rawBots) {
        return;
      }

      const parsed = JSON.parse(rawBots);
      if (Array.isArray(parsed)) {
        setCustomBots(parsed as CustomBotRecord[]);
      }
    } catch {
      window.localStorage.removeItem(storageKey);
    }
  }, [storageKey]);

  useEffect(() => {
    window.localStorage.setItem(storageKey, JSON.stringify(customBots));
  }, [customBots, storageKey]);

  useEffect(() => {
    let cancelled = false;

    const loadOfficialBot = async () => {
      try {
        const response = await apiFetch("/api/telegram/status", { cache: "no-store" });
        if (!response.ok) {
          throw new Error(`Backend returned ${response.status}`);
        }

        const payload = (await response.json()) as OfficialBotStatus;
        if (!cancelled) {
          setOfficialBot(payload);
          setOfficialBotError("");
        }
      } catch (error) {
        if (!cancelled) {
          setOfficialBot(null);
          setOfficialBotError(
            error instanceof Error ? error.message : "Не удалось загрузить статус бота"
          );
        }
      }
    };

    void loadOfficialBot();

    return () => {
      cancelled = true;
    };
  }, []);

  const applyTemplate = (templateId: BotTemplateId, origin: BotOrigin = "template") => {
    const template = getTemplate(templateId);
    const nextName =
      origin === "company"
        ? `Клон ${officialBot?.botName ?? "Trade360Lab Bot"}`
        : template.defaultName;

    startTransition(() => {
      setDraft({
        name: nextName,
        templateId: template.id,
        channel: template.defaultChannel,
        market: template.defaultMarket,
      });
      setDraftOrigin(origin);
    });
  };

  const createBot = () => {
    const name = draft.name.trim();
    if (!name) {
      return;
    }

    const template = getTemplate(draft.templateId);
    const createdAt = new Date().toISOString();

    const newBot: CustomBotRecord = {
      id:
        typeof crypto !== "undefined" && "randomUUID" in crypto
          ? crypto.randomUUID()
          : `bot-${Date.now()}`,
      name,
      templateId: template.id,
      channel: draft.channel,
      market: draft.market,
      origin: draftOrigin,
      summary:
        draftOrigin === "company"
          ? `Создан на базе встроенного ${officialBot?.botName ?? "Trade360Lab Bot"}`
          : template.description,
      createdAt,
    };

    startTransition(() => {
      setCustomBots((current) => [newBot, ...current]);
      setDraft({
        name: template.defaultName,
        templateId: template.id,
        channel: template.defaultChannel,
        market: template.defaultMarket,
      });
      setDraftOrigin("template");
    });
  };

  const loadIntoDraft = (bot: CustomBotRecord) => {
    startTransition(() => {
      setDraft({
        name: `${bot.name} copy`,
        templateId: bot.templateId,
        channel: bot.channel,
        market: bot.market,
      });
      setDraftOrigin(bot.origin);
    });
  };

  const removeBot = (botId: string) => {
    startTransition(() => {
      setCustomBots((current) => current.filter((bot) => bot.id !== botId));
    });
  };

  return (
    <div className="flex min-h-full flex-col gap-5">
      <PageHeader
        eyebrow="Боты"
        title="Боты"
        description="Единая панель для быстрого создания пользовательских ботов и контроля встроенного Telegram-бота компании."
        actions={
          <>
            <Button asChild>
              <Link href="/backtests">Открыть мониторинг</Link>
            </Button>
            <Button variant="outline" onClick={() => applyTemplate("monitoring")}>
              Создать из шаблона
            </Button>
          </>
        }
      />

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <SurfaceCard className="py-0" contentClassName="flex min-h-[108px] flex-col justify-between px-5 py-4">
          <div className="text-[11px] uppercase tracking-[0.24em] text-muted-foreground">
            Шаблоны
          </div>
          <div className="text-3xl font-semibold text-foreground">{botTemplates.length}</div>
          <div className="text-xs text-muted-foreground">
            Быстрые сценарии для пользовательских ботов
          </div>
        </SurfaceCard>
        <SurfaceCard className="py-0" contentClassName="flex min-h-[108px] flex-col justify-between px-5 py-4">
          <div className="text-[11px] uppercase tracking-[0.24em] text-muted-foreground">
            Пользовательские
          </div>
          <div className="text-3xl font-semibold text-foreground">{customBots.length}</div>
          <div className="text-xs text-muted-foreground">
            Сохраняются локально и быстро клонируются в черновик
          </div>
        </SurfaceCard>
        <SurfaceCard className="py-0" contentClassName="flex min-h-[108px] flex-col justify-between px-5 py-4">
          <div className="text-[11px] uppercase tracking-[0.24em] text-muted-foreground">
            Бот компании
          </div>
          <div className="text-2xl font-semibold text-foreground">
            {companyBotReady ? "Подключен" : companyBotBootable ? "Готов к запуску" : "Требует env"}
          </div>
          <div className="text-xs text-muted-foreground">
            Статус берётся из Java API, а не из заглушки во фронте
          </div>
        </SurfaceCard>
        <SurfaceCard className="py-0" contentClassName="flex min-h-[108px] flex-col justify-between px-5 py-4">
          <div className="text-[11px] uppercase tracking-[0.24em] text-muted-foreground">
            Команды
          </div>
          <div className="text-3xl font-semibold text-foreground">
            {officialBot?.commands.length ?? 5}
          </div>
          <div className="text-xs text-muted-foreground">
            `/runs`, `/last`, `/run &lt;id&gt;`, `/help`, `/settings`
          </div>
        </SurfaceCard>
      </div>

      <div className="grid gap-4 xl:grid-cols-[minmax(0,1.1fr)_minmax(360px,0.9fr)]">
        <SurfaceCard
          title="Trade360Lab Bot"
          subtitle="Встроенный Telegram-бот компании, уже присутствующий в backend/java"
          actions={
            <span
              className={cn(
                "inline-flex h-8 items-center rounded-full border px-3 text-xs font-semibold",
                companyBotReady
                  ? "border-[hsl(var(--tl-success)/0.25)] bg-[hsl(var(--tl-success)/0.12)] text-[hsl(var(--tl-success))]"
                  : companyBotBootable
                    ? "border-[hsl(var(--tl-warning)/0.25)] bg-[hsl(var(--tl-warning)/0.12)] text-[hsl(var(--tl-warning))]"
                    : "border-border/80 bg-[hsl(var(--tl-bg-2)/0.72)] text-muted-foreground"
              )}
            >
              {companyBotReady ? "Уведомления активны" : companyBotBootable ? "Можно запускать" : "Ожидает настройки"}
            </span>
          }
        >
          <div className="grid gap-5 lg:grid-cols-[minmax(0,1fr)_260px]">
            <div>
              <div className="flex items-start gap-4">
                <div className="relative flex h-16 w-16 shrink-0 items-center justify-center rounded-[20px] border border-[hsl(var(--primary)/0.18)] bg-[linear-gradient(160deg,hsl(var(--primary)/0.14),hsl(var(--accent)/0.1)_72%,hsl(var(--tl-bg-1)/0.98))] shadow-[0_16px_36px_rgba(0,0,0,0.12)]">
                  <Image src="/bot.png" alt="Trade360Lab Bot" width={36} height={36} className="h-9 w-9" />
                </div>
                <div className="min-w-0">
                  <div className="text-xl font-semibold tracking-tight text-foreground">
                    {officialBot?.botName ?? "Trade360Lab Bot"}
                  </div>
                  <div className="mt-1 text-sm text-muted-foreground">
                    {officialBot?.botUserName
                      ? `Username: ${officialBot.botUserName}`
                      : "Username Telegram пока не задан в конфигурации"}
                  </div>
                  <div className="mt-3 flex flex-wrap gap-2">
                    {(officialBot?.commands ?? ["/runs", "/last", "/run <id>", "/help", "/settings"]).map(
                      (command) => (
                        <span
                          key={command}
                          className="rounded-full border border-border/80 bg-[hsl(var(--tl-bg-2)/0.72)] px-3 py-1 text-xs text-foreground"
                        >
                          {command}
                        </span>
                      )
                    )}
                  </div>
                </div>
              </div>

              {officialBotError ? (
                <div className="mt-4 rounded-[18px] border border-[hsl(var(--tl-warning)/0.22)] bg-[hsl(var(--tl-warning)/0.1)] px-4 py-3 text-sm text-foreground">
                  Не удалось получить live-статус встроенного бота: {officialBotError}
                </div>
              ) : null}

              <div className="mt-5 grid gap-3 md:grid-cols-3">
                <div className="rounded-[18px] border border-border/80 bg-[hsl(var(--tl-bg-2)/0.72)] p-4">
                  <div className="text-[11px] uppercase tracking-[0.22em] text-muted-foreground">
                    Запуск long polling
                  </div>
                  <div className="mt-2 text-base font-semibold text-foreground">
                    {officialBot?.botStartupEnabled ? "Готов" : "Не готов"}
                  </div>
                </div>
                <div className="rounded-[18px] border border-border/80 bg-[hsl(var(--tl-bg-2)/0.72)] p-4">
                  <div className="text-[11px] uppercase tracking-[0.22em] text-muted-foreground">
                    Уведомления
                  </div>
                  <div className="mt-2 text-base font-semibold text-foreground">
                    {officialBot?.notificationsEnabled ? "Включены" : "Не настроены"}
                  </div>
                </div>
                <div className="rounded-[18px] border border-border/80 bg-[hsl(var(--tl-bg-2)/0.72)] p-4">
                  <div className="text-[11px] uppercase tracking-[0.22em] text-muted-foreground">
                    Конфигурация
                  </div>
                  <div className="mt-2 text-base font-semibold text-foreground">
                    {officialBot?.tokenConfigured ? "Token есть" : "Нужен token"}
                  </div>
                </div>
              </div>

              <div className="mt-5 flex flex-wrap gap-2">
                <Button onClick={() => applyTemplate("monitoring", "company")}>
                  Использовать как основу
                </Button>
                <Button variant="outline" asChild>
                  <Link href="/backtests">
                    Мониторинг запусков
                    <ArrowUpRight className="h-4 w-4" />
                  </Link>
                </Button>
              </div>
            </div>

            <div className="rounded-[22px] border border-border/80 bg-[linear-gradient(155deg,hsl(var(--tl-bg-1)/0.98),hsl(var(--tl-bg-2)/0.94))] p-4 shadow-[0_16px_36px_rgba(0,0,0,0.12)]">
              <div className="text-sm font-semibold text-foreground">Что уже умеет бот компании</div>
              <div className="mt-4 space-y-3">
                <div className="flex items-start gap-3 rounded-[16px] border border-border/80 bg-[hsl(var(--tl-bg-1)/0.72)] p-3">
                  <Command className="mt-0.5 h-4 w-4 shrink-0 text-[#2BD576]" />
                  <div>
                    <div className="text-sm font-medium text-foreground">Команды для run-режима</div>
                    <div className="text-xs text-muted-foreground">
                      Последние прогоны, lookup по `run id`, настройки уведомлений.
                    </div>
                  </div>
                </div>
                <div className="flex items-start gap-3 rounded-[16px] border border-border/80 bg-[hsl(var(--tl-bg-1)/0.72)] p-3">
                  <ShieldCheck className="mt-0.5 h-4 w-4 shrink-0 text-[#2BD576]" />
                  <div>
                    <div className="text-sm font-medium text-foreground">События из backend</div>
                    <div className="text-xs text-muted-foreground">
                      Старт, завершение и ошибки запусков уже встроены в Java API.
                    </div>
                  </div>
                </div>
                <div className="flex items-start gap-3 rounded-[16px] border border-border/80 bg-[hsl(var(--tl-bg-1)/0.72)] p-3">
                  <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0 text-[#2BD576]" />
                  <div>
                    <div className="text-sm font-medium text-foreground">Готовая база для клонов</div>
                    <div className="text-xs text-muted-foreground">
                      Его можно использовать как основу для пользовательских ботов из этой же панели.
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </SurfaceCard>

        <SurfaceCard title="Быстрое создание своего бота" subtitle="Минимум полей, шаблонная база и мгновенный черновик">
          <div className="space-y-4">
            <div className="grid gap-3">
              {botTemplates.map((template) => {
                const TemplateIcon = template.icon;
                const isActive = template.id === draft.templateId && draftOrigin === "template";

                return (
                  <button
                    key={template.id}
                    type="button"
                    onClick={() => applyTemplate(template.id)}
                    className={cn(
                      "text-left rounded-[20px] border p-4 transition-all duration-200",
                      isActive
                        ? "border-[hsl(var(--primary)/0.28)] bg-[linear-gradient(160deg,hsl(var(--primary)/0.12),hsl(var(--accent)/0.08)_70%,hsl(var(--tl-bg-1)/0.96))] shadow-[0_16px_32px_rgba(0,0,0,0.12)]"
                        : "border-border/80 bg-[linear-gradient(155deg,hsl(var(--tl-bg-1)/0.98),hsl(var(--tl-bg-2)/0.94))] hover:border-[hsl(var(--primary)/0.22)]"
                    )}
                  >
                    <div className="flex items-start gap-3">
                      <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-[14px] border border-border/80 bg-[hsl(var(--tl-bg-1)/0.72)]">
                        <TemplateIcon className="h-4 w-4 text-[#2BD576]" />
                      </div>
                      <div>
                        <div className="text-sm font-semibold text-foreground">{template.title}</div>
                        <div className="mt-1 text-xs text-muted-foreground">
                          {template.description}
                        </div>
                      </div>
                    </div>
                  </button>
                );
              })}
            </div>

            <div className="rounded-[22px] border border-border/80 bg-[hsl(var(--tl-bg-2)/0.62)] p-4">
              <div className="grid gap-3">
                <div>
                  <div className="mb-1 text-xs text-muted-foreground">Название бота</div>
                  <Input
                    value={draft.name}
                    onChange={(event) =>
                      setDraft((current) => ({ ...current, name: event.target.value }))
                    }
                    placeholder="Например, BTC Signal Desk"
                  />
                </div>
                <div className="grid gap-3 sm:grid-cols-3">
                  <div>
                    <div className="mb-1 text-xs text-muted-foreground">Шаблон</div>
                    <Select
                      value={draft.templateId}
                      onValueChange={(value) => applyTemplate(value as BotTemplateId)}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {botTemplates.map((template) => (
                          <SelectItem key={template.id} value={template.id}>
                            {template.title}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  <div>
                    <div className="mb-1 text-xs text-muted-foreground">Канал</div>
                    <Select
                      value={draft.channel}
                      onValueChange={(value) =>
                        setDraft((current) => ({ ...current, channel: value as BotChannel }))
                      }
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {channelOptions.map((channel) => (
                          <SelectItem key={channel.value} value={channel.value}>
                            {channel.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  <div>
                    <div className="mb-1 text-xs text-muted-foreground">Рынок</div>
                    <Select
                      value={draft.market}
                      onValueChange={(value) =>
                        setDraft((current) => ({ ...current, market: value as BotMarket }))
                      }
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {marketOptions.map((market) => (
                          <SelectItem key={market.value} value={market.value}>
                            {market.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </div>
              </div>

              <div className="mt-4 rounded-[18px] border border-border/80 bg-[hsl(var(--tl-bg-1)/0.74)] p-4">
                <div className="text-xs uppercase tracking-[0.2em] text-muted-foreground">
                  Выбранный сценарий
                </div>
                <div className="mt-2 text-sm font-semibold text-foreground">
                  {selectedTemplate.title}
                </div>
                <div className="mt-1 text-xs text-muted-foreground">
                  {selectedTemplate.description}
                </div>
                <div className="mt-3 flex flex-wrap gap-2">
                  <span className="rounded-full border border-border/80 bg-[hsl(var(--tl-bg-2)/0.7)] px-3 py-1 text-xs text-foreground">
                    {getChannelLabel(draft.channel)}
                  </span>
                  <span className="rounded-full border border-border/80 bg-[hsl(var(--tl-bg-2)/0.7)] px-3 py-1 text-xs text-foreground">
                    {getMarketLabel(draft.market)}
                  </span>
                  <span className="rounded-full border border-border/80 bg-[hsl(var(--tl-bg-2)/0.7)] px-3 py-1 text-xs text-foreground">
                    {draftOrigin === "company" ? "Основа: бот компании" : "Основа: шаблон"}
                  </span>
                </div>
              </div>

              <div className="mt-4 flex flex-wrap gap-2">
                <Button onClick={createBot}>Создать бота</Button>
                <Button variant="outline" onClick={() => applyTemplate("monitoring", "company")}>
                  Клонировать официальный бот
                </Button>
              </div>
              <div className="mt-3 text-xs text-muted-foreground">
                Черновики сохраняются локально в браузере, поэтому создать и повторно открыть своего бота можно без ручной сборки заново.
              </div>
            </div>
          </div>
        </SurfaceCard>
      </div>

      <SurfaceCard
        title="Пользовательские боты"
        subtitle="Ваши собственные боты, собранные из шаблонов или на базе встроенного Trade360Lab Bot"
        actions={
          <div className="text-xs text-muted-foreground">
            Всего: {customBots.length}
          </div>
        }
      >
        {customBots.length > 0 ? (
          <div className="grid gap-4 xl:grid-cols-2">
            {customBots.map((bot) => {
              const template = getTemplate(bot.templateId);

              return (
                <div
                  key={bot.id}
                  className="rounded-[22px] border border-border/80 bg-[linear-gradient(155deg,hsl(var(--tl-bg-1)/0.98),hsl(var(--tl-bg-2)/0.94))] p-4 shadow-[0_16px_36px_rgba(0,0,0,0.12)]"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <div className="text-base font-semibold text-foreground">{bot.name}</div>
                      <div className="mt-1 text-xs text-muted-foreground">{bot.summary}</div>
                    </div>
                    <span
                      className={cn(
                        "inline-flex h-8 items-center rounded-full border px-3 text-xs font-semibold",
                        bot.origin === "company"
                          ? "border-[hsl(var(--primary)/0.22)] bg-[hsl(var(--primary)/0.12)] text-[hsl(var(--primary))]"
                          : "border-border/80 bg-[hsl(var(--tl-bg-2)/0.72)] text-muted-foreground"
                      )}
                    >
                      {bot.origin === "company" ? "На базе компании" : "Пользовательский"}
                    </span>
                  </div>

                  <div className="mt-4 grid gap-3 sm:grid-cols-2">
                    <div className="rounded-[16px] border border-border/80 bg-[hsl(var(--tl-bg-1)/0.72)] p-3">
                      <div className="text-[11px] uppercase tracking-[0.2em] text-muted-foreground">
                        Шаблон
                      </div>
                      <div className="mt-2 text-sm font-medium text-foreground">
                        {template.title}
                      </div>
                    </div>
                    <div className="rounded-[16px] border border-border/80 bg-[hsl(var(--tl-bg-1)/0.72)] p-3">
                      <div className="text-[11px] uppercase tracking-[0.2em] text-muted-foreground">
                        Канал / рынок
                      </div>
                      <div className="mt-2 text-sm font-medium text-foreground">
                        {getChannelLabel(bot.channel)} / {getMarketLabel(bot.market)}
                      </div>
                    </div>
                  </div>

                  <div className="mt-4 flex items-center justify-between gap-3 text-xs text-muted-foreground">
                    <div>Создан: {formatCreatedAt(bot.createdAt)}</div>
                    <div className="flex gap-2">
                      <Button size="sm" variant="outline" onClick={() => loadIntoDraft(bot)}>
                        <Copy className="h-3.5 w-3.5" />
                        В черновик
                      </Button>
                      <Button size="sm" variant="ghost" onClick={() => removeBot(bot.id)}>
                        <Trash2 className="h-3.5 w-3.5" />
                        Удалить
                      </Button>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="rounded-[22px] border border-dashed border-border bg-[hsl(var(--tl-bg-2)/0.58)] p-6">
            <div className="flex items-start gap-3">
              <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-[16px] border border-border/80 bg-[hsl(var(--tl-bg-1)/0.72)]">
                <Bot className="h-5 w-5 text-[#2BD576]" />
              </div>
              <div>
                <div className="text-sm font-semibold text-foreground">
                  Пока нет пользовательских ботов
                </div>
                <div className="mt-1 text-sm text-muted-foreground">
                  Выбери шаблон справа или возьми за основу встроенный Trade360Lab Bot, и новый черновик появится здесь.
                </div>
              </div>
            </div>
          </div>
        )}
      </SurfaceCard>
    </div>
  );
}
