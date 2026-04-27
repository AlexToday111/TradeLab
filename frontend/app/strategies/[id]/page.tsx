"use client";

import { ChangeEvent, useCallback, useEffect, useMemo, useRef, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { ArrowLeft, CheckCircle2, RefreshCw, Upload } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Textarea } from "@/components/ui/textarea";
import { EmptyState } from "@/components/shared/empty-state";
import { LoadingState } from "@/components/shared/loading-state";
import { SurfaceCard } from "@/components/shared/surface-card";
import {
  activateStrategyVersion,
  createStrategyPreset,
  fetchStrategyById,
  fetchStrategyPresets,
  fetchStrategyVersions,
  uploadStrategyVersion,
  validateStrategyVersion,
} from "@/lib/api/strategies";
import type { Strategy, StrategyPreset, StrategyVersion } from "@/lib/types";

type BadgeVariant = "default" | "secondary" | "destructive" | "outline";

function statusVariant(status: string): BadgeVariant {
  if (status === "VALID" || status === "ACTIVE") {
    return "default";
  }
  if (status === "INVALID" || status === "ARCHIVED") {
    return "destructive";
  }
  return "secondary";
}

function formatDate(value: string | null | undefined) {
  if (!value) {
    return "n/a";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return new Intl.DateTimeFormat("ru-RU", {
    day: "2-digit",
    month: "2-digit",
    year: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

function formatBytes(value: number | null | undefined) {
  if (!value || value <= 0) {
    return "0 B";
  }
  const units = ["B", "KB", "MB", "GB"];
  let amount = value;
  let index = 0;
  while (amount >= 1024 && index < units.length - 1) {
    amount /= 1024;
    index += 1;
  }
  return `${amount.toFixed(index === 0 ? 0 : 1)} ${units[index]}`;
}

function shortChecksum(value: string | null | undefined) {
  if (!value) {
    return "n/a";
  }
  return value.length > 16 ? `${value.slice(0, 16)}...` : value;
}

export default function StrategyDetailPage() {
  const params = useParams<{ id: string }>();
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const strategyId = params.id;

  const [strategy, setStrategy] = useState<Strategy | null>(null);
  const [versions, setVersions] = useState<StrategyVersion[]>([]);
  const [presets, setPresets] = useState<StrategyPreset[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isUploading, setIsUploading] = useState(false);
  const [isSavingPreset, setIsSavingPreset] = useState(false);
  const [presetName, setPresetName] = useState("Default");
  const [presetPayload, setPresetPayload] = useState("{\n  \n}");
  const [error, setError] = useState<string | null>(null);

  const latestVersion = useMemo(
    () => versions.find((version) => String(version.id) === String(strategy?.latestVersionId)),
    [strategy?.latestVersionId, versions]
  );

  const load = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [strategyResponse, versionList, presetList] = await Promise.all([
        fetchStrategyById(strategyId),
        fetchStrategyVersions(strategyId),
        fetchStrategyPresets(strategyId),
      ]);
      setStrategy(strategyResponse);
      setVersions(versionList);
      setPresets(presetList);
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Failed to load strategy");
    } finally {
      setIsLoading(false);
    }
  }, [strategyId]);

  useEffect(() => {
    void load();
  }, [load]);

  async function handleVersionFileSelect(event: ChangeEvent<HTMLInputElement>) {
    const selectedFile = event.target.files?.[0];
    event.target.value = "";
    if (!selectedFile) {
      return;
    }

    setIsUploading(true);
    setError(null);
    try {
      await uploadStrategyVersion(strategyId, selectedFile);
      await load();
    } catch (uploadError) {
      setError(uploadError instanceof Error ? uploadError.message : "Failed to upload version");
    } finally {
      setIsUploading(false);
    }
  }

  async function handleValidate(versionId: number | string) {
    setError(null);
    try {
      await validateStrategyVersion(versionId);
      await load();
    } catch (validationError) {
      setError(
        validationError instanceof Error ? validationError.message : "Failed to validate version"
      );
    }
  }

  async function handleActivate(versionId: number | string) {
    setError(null);
    try {
      await activateStrategyVersion(versionId);
      await load();
    } catch (activationError) {
      setError(
        activationError instanceof Error ? activationError.message : "Failed to activate version"
      );
    }
  }

  async function handleCreatePreset() {
    setIsSavingPreset(true);
    setError(null);
    try {
      const payload = JSON.parse(presetPayload) as Record<string, unknown>;
      await createStrategyPreset(strategyId, {
        name: presetName,
        presetPayload: payload,
      });
      setPresetPayload("{\n  \n}");
      await load();
    } catch (presetError) {
      setError(presetError instanceof Error ? presetError.message : "Failed to create preset");
    } finally {
      setIsSavingPreset(false);
    }
  }

  if (isLoading) {
    return (
      <div className="flex min-h-full flex-col gap-5">
        <LoadingState label="Загрузка стратегии" />
      </div>
    );
  }

  if (!strategy) {
    return (
      <SurfaceCard>
        <EmptyState
          title="Стратегия не найдена"
          description="Запись недоступна текущему пользователю."
          actionLabel="К стратегиям"
          actionHref="/strategies"
        />
      </SurfaceCard>
    );
  }

  return (
    <div className="flex min-h-full flex-col gap-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <Button variant="ghost" asChild>
          <Link href="/strategies">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Стратегии
          </Link>
        </Button>
        <input
          ref={fileInputRef}
          type="file"
          accept=".py"
          className="hidden"
          onChange={handleVersionFileSelect}
        />
        <Button onClick={() => fileInputRef.current?.click()} disabled={isUploading}>
          <Upload className="mr-2 h-4 w-4" />
          {isUploading ? "Загрузка" : "Новая версия"}
        </Button>
      </div>

      {error ? (
        <SurfaceCard>
          <div className="text-sm text-destructive">{error}</div>
        </SurfaceCard>
      ) : null}

      <SurfaceCard title={strategy.name ?? strategy.fileName} subtitle={strategy.strategyKey ?? ""}>
        <div className="grid gap-3 md:grid-cols-4">
          <div className="rounded-[18px] border border-border/70 bg-panel-subtle p-4">
            <div className="text-xs text-muted-foreground">Lifecycle</div>
            <Badge className="mt-2" variant={statusVariant(strategy.lifecycleStatus)}>
              {strategy.lifecycleStatus}
            </Badge>
          </div>
          <div className="rounded-[18px] border border-border/70 bg-panel-subtle p-4">
            <div className="text-xs text-muted-foreground">Validation</div>
            <Badge className="mt-2" variant={statusVariant(strategy.status)}>
              {strategy.status}
            </Badge>
          </div>
          <div className="rounded-[18px] border border-border/70 bg-panel-subtle p-4">
            <div className="text-xs text-muted-foreground">Latest version</div>
            <div className="mt-2 text-sm font-semibold text-foreground">
              {strategy.latestVersion ?? "n/a"}
            </div>
          </div>
          <div className="rounded-[18px] border border-border/70 bg-panel-subtle p-4">
            <div className="text-xs text-muted-foreground">Checksum</div>
            <div className="mt-2 font-mono text-xs text-foreground">
              {shortChecksum(strategy.checksum)}
            </div>
          </div>
        </div>

        {latestVersion ? (
          <pre className="mt-4 max-h-44 overflow-auto rounded-xl bg-background/60 p-3 text-xs text-muted-foreground">
            {JSON.stringify(latestVersion.validationReport ?? {}, null, 2)}
          </pre>
        ) : null}
      </SurfaceCard>

      <SurfaceCard title="Versions" subtitle="Immutable source snapshots">
        {versions.length === 0 ? (
          <EmptyState title="Версий нет" description="Загрузите файл версии стратегии." />
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Version</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>File</TableHead>
                <TableHead>Size</TableHead>
                <TableHead>Checksum</TableHead>
                <TableHead>Created</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {versions.map((version) => (
                <TableRow key={String(version.id)}>
                  <TableCell className="font-semibold">{version.version}</TableCell>
                  <TableCell>
                    <Badge variant={statusVariant(version.validationStatus)}>
                      {version.validationStatus}
                    </Badge>
                  </TableCell>
                  <TableCell>{version.fileName}</TableCell>
                  <TableCell>{formatBytes(version.sizeBytes)}</TableCell>
                  <TableCell className="font-mono text-xs">{shortChecksum(version.checksum)}</TableCell>
                  <TableCell>{formatDate(version.createdAt)}</TableCell>
                  <TableCell>
                    <div className="flex justify-end gap-2">
                      <Button
                        size="sm"
                        variant="secondary"
                        onClick={() => handleValidate(version.id)}
                      >
                        <RefreshCw className="mr-2 h-3.5 w-3.5" />
                        Validate
                      </Button>
                      <Button
                        size="sm"
                        onClick={() => handleActivate(version.id)}
                        disabled={
                          version.validationStatus !== "VALID" &&
                          version.validationStatus !== "WARNING"
                        }
                      >
                        <CheckCircle2 className="mr-2 h-3.5 w-3.5" />
                        Activate
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </SurfaceCard>

      <SurfaceCard title="Presets" subtitle="Reusable parameter payloads">
        <div className="grid gap-4 lg:grid-cols-[1fr_1.2fr]">
          <div className="space-y-3">
            <Input value={presetName} onChange={(event) => setPresetName(event.target.value)} />
            <Textarea
              value={presetPayload}
              onChange={(event) => setPresetPayload(event.target.value)}
              className="min-h-[160px] font-mono text-xs"
            />
            <Button
              onClick={handleCreatePreset}
              disabled={isSavingPreset || !presetName.trim() || !presetPayload.trim()}
            >
              {isSavingPreset ? "Сохранение" : "Сохранить preset"}
            </Button>
          </div>
          <div className="grid gap-3">
            {presets.length === 0 ? (
              <EmptyState title="Preset нет" description="Сохраненные параметры появятся здесь." />
            ) : (
              presets.map((preset) => (
                <div
                  key={String(preset.id)}
                  className="rounded-[18px] border border-border/70 bg-panel-subtle p-4"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <div className="text-sm font-semibold text-foreground">{preset.name}</div>
                      <div className="mt-1 text-xs text-muted-foreground">
                        {formatDate(preset.updatedAt)}
                      </div>
                    </div>
                    <Badge variant="secondary">#{preset.id}</Badge>
                  </div>
                  <pre className="mt-3 max-h-32 overflow-auto rounded-xl bg-background/60 p-3 text-xs text-muted-foreground">
                    {JSON.stringify(preset.presetPayload, null, 2)}
                  </pre>
                </div>
              ))
            )}
          </div>
        </div>
      </SurfaceCard>
    </div>
  );
}
