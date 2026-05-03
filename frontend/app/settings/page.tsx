"use client";

import { ReactNode, useEffect, useMemo, useState } from "react";
import { AlertTriangle, CheckCircle2, RefreshCw, ShieldAlert, Wifi } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/shared/empty-state";
import { LoadingState } from "@/components/shared/loading-state";
import { PageHeader } from "@/components/shared/page-header";
import { SurfaceCard } from "@/components/shared/surface-card";
import { apiFetch } from "@/lib/api/client";
import { releaseInfo } from "@/lib/release";
import { cn } from "@/lib/utils";

type JavaHealth = {
  status: string;
  service: string;
};

type PythonHealth = {
  status: string;
  service: string;
};

type LiveRiskStatus = {
  killSwitchActive: boolean;
  killSwitchReason?: string | null;
  circuitBreakers: { exchange: string; active: boolean; reason?: string | null }[];
};

type ExchangeHealth = {
  exchange: string;
  connected: boolean;
  credentialsValid: boolean;
  realOrderSubmissionEnabled: boolean;
  message: string;
};

type ServiceState = "ok" | "warn" | "error";

async function readJson<T>(response: Response): Promise<T> {
  const payload = await response.json();
  if (!response.ok) {
    throw new Error(payload?.message ?? "Request failed");
  }
  return payload as T;
}

export default function SettingsPage() {
  const [javaHealth, setJavaHealth] = useState<JavaHealth | null>(null);
  const [pythonHealth, setPythonHealth] = useState<PythonHealth | null>(null);
  const [liveRisk, setLiveRisk] = useState<LiveRiskStatus | null>(null);
  const [exchangeHealth, setExchangeHealth] = useState<ExchangeHealth | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  async function refresh() {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const [javaData, pythonData, riskData, exchangeData] = await Promise.all([
        readJson<JavaHealth>(await apiFetch("/api/health")),
        readJson<PythonHealth>(await apiFetch("/api/python/health")),
        readJson<LiveRiskStatus>(await apiFetch("/api/live/risk/status")),
        readJson<ExchangeHealth>(await apiFetch("/api/live/exchange/health?exchange=binance")),
      ]);
      setJavaHealth(javaData);
      setPythonHealth(pythonData);
      setLiveRisk(riskData);
      setExchangeHealth(exchangeData);
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Service health check failed");
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    refresh();
  }, []);

  const circuitBreakerActive = useMemo(
    () => liveRisk?.circuitBreakers.some((breaker) => breaker.active) ?? false,
    [liveRisk]
  );

  return (
    <div className="flex min-h-full flex-col gap-5">
      <PageHeader
        eyebrow="Release operations"
        title="Service Health"
        description="Release status, service reachability, and live trading safety state."
        actions={
          <Button variant="secondary" size="sm" onClick={refresh} disabled={isLoading}>
            <RefreshCw className="mr-2 h-4 w-4" />
            Refresh
          </Button>
        }
      />

      {errorMessage ? (
        <div className="rounded-[16px] border border-status-error/35 bg-status-error/10 px-4 py-3 text-sm text-status-error">
          {errorMessage}
        </div>
      ) : null}

      {isLoading && !javaHealth ? <LoadingState label="Checking services..." /> : null}

      <div className="grid gap-5 xl:grid-cols-[360px_1fr]">
        <SurfaceCard title="Release" subtitle={releaseInfo.name}>
          <div className="grid gap-3">
            <Metric label="Version" value={`v${releaseInfo.version}`} />
            <Metric
              label="Live mode"
              value={exchangeHealth?.realOrderSubmissionEnabled ? "Real submission enabled" : "Guarded testnet mode"}
              state={exchangeHealth?.realOrderSubmissionEnabled ? "warn" : "ok"}
            />
            <Metric label="Binance" value={exchangeHealth?.message ?? "Not checked"} />
          </div>
        </SurfaceCard>

        <SurfaceCard title="Services">
          <div className="grid gap-3 md:grid-cols-3">
            <ServiceTile
              title="Frontend"
              detail={`v${releaseInfo.version}`}
              state="ok"
              icon={<Wifi className="h-4 w-4" />}
            />
            <ServiceTile
              title={javaHealth?.service ?? "java-api"}
              detail={javaHealth?.status ?? "unknown"}
              state={javaHealth?.status === "ok" ? "ok" : "error"}
              icon={<CheckCircle2 className="h-4 w-4" />}
            />
            <ServiceTile
              title={pythonHealth?.service ?? "python-parser"}
              detail={pythonHealth?.status ?? "unknown"}
              state={pythonHealth?.status === "ok" ? "ok" : "error"}
              icon={<CheckCircle2 className="h-4 w-4" />}
            />
          </div>
        </SurfaceCard>
      </div>

      <SurfaceCard title="Live Trading Safety">
        <div className="grid gap-3 md:grid-cols-4">
          <Metric
            label="Kill switch"
            value={liveRisk?.killSwitchActive ? "ACTIVE" : "Clear"}
            state={liveRisk?.killSwitchActive ? "error" : "ok"}
          />
          <Metric
            label="Circuit breaker"
            value={circuitBreakerActive ? "ACTIVE" : "Clear"}
            state={circuitBreakerActive ? "error" : "ok"}
          />
          <Metric
            label="Credentials"
            value={exchangeHealth?.credentialsValid ? "Valid" : "Missing or invalid"}
            state={exchangeHealth?.credentialsValid ? "ok" : "warn"}
          />
          <Metric
            label="Submission"
            value={exchangeHealth?.realOrderSubmissionEnabled ? "Enabled" : "Disabled by default"}
            state={exchangeHealth?.realOrderSubmissionEnabled ? "warn" : "ok"}
          />
        </div>
        {liveRisk?.killSwitchReason ? (
          <div className="mt-4 rounded-[16px] border border-status-warning/35 bg-status-warning/10 px-4 py-3 text-sm text-status-warning">
            {liveRisk.killSwitchReason}
          </div>
        ) : null}
        {liveRisk && liveRisk.circuitBreakers.length === 0 ? (
          <div className="mt-4">
            <EmptyState title="No circuit breaker state" description="No exchange circuit breakers have been recorded." />
          </div>
        ) : null}
      </SurfaceCard>
    </div>
  );
}

function ServiceTile({
  title,
  detail,
  state,
  icon,
}: {
  title: string;
  detail: string;
  state: ServiceState;
  icon: ReactNode;
}) {
  return (
    <div className="rounded-[16px] border border-border/70 bg-panel-subtle p-4">
      <div className="flex items-center justify-between gap-3">
        <div className="flex items-center gap-2 text-sm font-semibold text-foreground">
          {icon}
          {title}
        </div>
        <StateBadge state={state} />
      </div>
      <div className="mt-3 text-xs text-muted-foreground">{detail}</div>
    </div>
  );
}

function Metric({
  label,
  value,
  state = "ok",
}: {
  label: string;
  value: ReactNode;
  state?: ServiceState;
}) {
  return (
    <div className="rounded-[16px] border border-border/70 bg-panel-subtle px-4 py-3">
      <div className="text-xs text-muted-foreground">{label}</div>
      <div
        className={cn(
          "mt-2 text-sm font-semibold",
          state === "error" ? "text-status-error" : state === "warn" ? "text-status-warning" : "text-foreground"
        )}
      >
        {value}
      </div>
    </div>
  );
}

function StateBadge({ state }: { state: ServiceState }) {
  const label = state === "ok" ? "OK" : state === "warn" ? "WARN" : "ERROR";
  return (
    <Badge
      variant="outline"
      className={cn(
        "rounded-full px-2.5 py-1",
        state === "ok" && "border-status-success/35 bg-status-success/12 text-status-success",
        state === "warn" && "border-status-warning/35 bg-status-warning/12 text-status-warning",
        state === "error" && "border-status-error/35 bg-status-error/12 text-status-error"
      )}
    >
      {state === "error" ? <ShieldAlert className="mr-1 h-3 w-3" /> : null}
      {state === "warn" ? <AlertTriangle className="mr-1 h-3 w-3" /> : null}
      {label}
    </Badge>
  );
}
