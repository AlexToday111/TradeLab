"use client";

import { Check, MoonStar, SunMedium } from "lucide-react";
import { PageHeader } from "@/components/shared/page-header";
import {
  interfaceThemeOptions,
  useTheme,
  type InterfaceTheme,
} from "@/components/theme/theme-provider";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Switch } from "@/components/ui/switch";
import { cn } from "@/lib/utils";

const themeMeta: Record<
  InterfaceTheme,
  {
    title: string;
    description: string;
    previewClassName: string;
    previewToneClassName: string;
  }
> = {
  black: {
    title: "Black",
    description: "Глубокий тёмный интерфейс для работы в фокусе.",
    previewClassName:
      "border-white/10 bg-[linear-gradient(160deg,#0e121a,#07090f)] text-white/88",
    previewToneClassName: "bg-[#2BD576]",
  },
  white: {
    title: "White",
    description: "Светлая поверхность с теми же зелёными акцентами.",
    previewClassName:
      "border-[hsl(var(--tl-border-1)/0.8)] bg-[linear-gradient(160deg,#ffffff,#eef5ef)] text-[hsl(var(--foreground))]",
    previewToneClassName: "bg-[#2BD576]",
  },
};

export default function SettingsPage() {
  const { theme, setTheme } = useTheme();
  const isLightTheme = theme === "white";

  return (
    <div className="flex h-full flex-col gap-5">
      <PageHeader
        eyebrow="Настройки"
        title="Настройки"
        description="Управление темой интерфейса и базовыми параметрами рабочей среды."
      />
      <div className="grid gap-5 xl:grid-cols-[minmax(0,1.2fr)_minmax(320px,0.8fr)]">
        <Card className="overflow-hidden">
          <CardHeader className="gap-4 border-b border-[hsl(var(--tl-border-1)/0.62)] bg-[linear-gradient(145deg,hsl(var(--primary)/0.1),hsl(var(--accent)/0.06)_48%,hsl(var(--tl-bg-1)/0.92))]">
            <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
              <div className="space-y-1">
                <CardTitle>Тема интерфейса</CardTitle>
                <CardDescription>
                  Переключение сохраняется локально и применяется ко всем основным экранам.
                </CardDescription>
              </div>
              <div className="flex items-center gap-3 rounded-full border border-[hsl(var(--tl-border-1)/0.8)] bg-[hsl(var(--tl-bg-1)/0.9)] px-3 py-2">
                <MoonStar
                  className={cn(
                    "h-4 w-4 transition-colors",
                    isLightTheme ? "text-muted-foreground" : "text-foreground"
                  )}
                />
                <Switch
                  checked={isLightTheme}
                  onCheckedChange={(checked) => setTheme(checked ? "white" : "black")}
                  aria-label="Переключить тему интерфейса"
                />
                <SunMedium
                  className={cn(
                    "h-4 w-4 transition-colors",
                    isLightTheme ? "text-foreground" : "text-muted-foreground"
                  )}
                />
              </div>
            </div>
          </CardHeader>
          <CardContent className="grid gap-4 pt-6 md:grid-cols-2">
            {interfaceThemeOptions.map((option) => {
              const selected = theme === option.value;
              const meta = themeMeta[option.value];

              return (
                <button
                  key={option.value}
                  type="button"
                  onClick={() => setTheme(option.value)}
                  aria-pressed={selected}
                  className={cn(
                    "group rounded-[22px] border p-4 text-left transition-all duration-200",
                    selected
                      ? "border-[#c7ee51]/45 bg-[#c7ee51]/10 shadow-[inset_0_1px_0_rgba(255,255,255,0.08),0_0_0_1px_rgba(201,239,78,0.08)]"
                      : "border-[hsl(var(--tl-border-1)/0.72)] bg-[linear-gradient(180deg,hsl(var(--tl-bg-1)/0.98),hsl(var(--tl-bg-2)/0.92))] hover:border-[hsl(var(--primary)/0.24)] hover:bg-[hsl(var(--tl-bg-2)/0.84)]"
                  )}
                >
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <div className="text-sm font-semibold text-foreground">{meta.title}</div>
                      <div className="mt-1 text-xs leading-relaxed text-muted-foreground">
                        {meta.description}
                      </div>
                    </div>
                    <div
                      className={cn(
                        "inline-flex h-7 w-7 items-center justify-center rounded-full border transition-colors",
                        selected
                          ? "border-[#c7ee51]/45 bg-[#c7ee51]/12 text-[#c7ee51]"
                          : "border-[hsl(var(--tl-border-1)/0.9)] text-muted-foreground"
                      )}
                    >
                      <Check className="h-4 w-4" />
                    </div>
                  </div>

                  <div
                    className={cn(
                      "mt-4 rounded-[18px] border p-3 shadow-[inset_0_1px_0_rgba(255,255,255,0.08)]",
                      meta.previewClassName
                    )}
                  >
                    <div className="flex items-center justify-between text-[11px] uppercase tracking-[0.2em]">
                      <span>{meta.title}</span>
                      <span>Preview</span>
                    </div>
                    <div className="mt-3 grid grid-cols-[1.3fr_0.7fr] gap-2">
                      <div className="rounded-[14px] border border-current/10 bg-current/5 p-3">
                        <div className="h-2 w-16 rounded-full bg-current/75" />
                        <div className="mt-2 h-2 w-24 rounded-full bg-current/30" />
                        <div className="mt-4 h-8 rounded-[10px] bg-current/10" />
                      </div>
                      <div className="space-y-2">
                        <div className="rounded-[14px] border border-current/10 bg-current/5 p-3">
                          <div
                            className={cn("h-2.5 w-10 rounded-full", meta.previewToneClassName)}
                          />
                          <div className="mt-3 h-2 rounded-full bg-current/20" />
                          <div className="mt-2 h-2 w-3/4 rounded-full bg-current/20" />
                        </div>
                        <div className="rounded-[14px] border border-current/10 bg-current/5 p-3">
                          <div className="flex gap-1.5">
                            <span className={cn("h-2.5 w-2.5 rounded-full", meta.previewToneClassName)} />
                            <span className="h-2.5 w-2.5 rounded-full bg-current/25" />
                            <span className="h-2.5 w-2.5 rounded-full bg-current/15" />
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </button>
              );
            })}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Поведение темы</CardTitle>
            <CardDescription>
              Светлый и тёмный режимы используют одни и те же зелёные акценты и индикаторы.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-3 text-sm text-muted-foreground">
            <div className="rounded-[18px] border border-[hsl(var(--tl-border-1)/0.72)] bg-[hsl(var(--tl-bg-2)/0.74)] p-4">
              Переключение доступно здесь и в выпадающем меню верхней панели.
            </div>
            <div className="rounded-[18px] border border-[hsl(var(--tl-border-1)/0.72)] bg-[hsl(var(--tl-bg-2)/0.74)] p-4">
              Выбранная тема сохраняется в браузере и подхватывается при следующем открытии приложения.
            </div>
            <div className="rounded-[18px] border border-[hsl(var(--tl-border-1)/0.72)] bg-[hsl(var(--tl-bg-2)/0.74)] p-4">
              Зелёные CTA и статусные акценты оставлены без изменения, меняется только светлота интерфейса.
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
