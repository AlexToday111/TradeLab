import type { Strategy, StrategyPreset, StrategyTemplate, StrategyVersion } from "@/lib/types";
import { apiFetch } from "@/lib/api/client";

async function readErrorMessage(response: Response) {
  try {
    const payload = (await response.json()) as { message?: string };
    if (typeof payload.message === "string" && payload.message.trim().length > 0) {
      return payload.message;
    }
  } catch {
    // Ignore JSON parsing issues and fallback to text/status below.
  }

  try {
    const text = await response.text();
    if (text.trim().length > 0) {
      return text;
    }
  } catch {
    // Ignore text parsing issues and fallback to status below.
  }

  return `Request failed with status ${response.status}`;
}

function toStrategies(payload: unknown): Strategy[] {
  if (Array.isArray(payload)) {
    return payload
      .map((item) => toStrategy(item))
      .filter((item): item is Strategy => item !== null);
  }

  return [];
}

function numberOrString(value: unknown) {
  return typeof value === "number" || typeof value === "string" ? value : null;
}

function objectOrNull(value: unknown) {
  return value && typeof value === "object" && !Array.isArray(value)
    ? (value as Record<string, unknown>)
    : null;
}

function stringList(value: unknown) {
  return Array.isArray(value)
    ? value.filter((item): item is string => typeof item === "string")
    : [];
}

function toStrategy(payload: unknown): Strategy | null {
  if (!payload || typeof payload !== "object") {
    return null;
  }

  const candidate = payload as Record<string, unknown>;
  const fileName = candidate.fileName;

  if (typeof fileName !== "string") {
    return null;
  }

  return {
    id: typeof candidate.id === "number" || typeof candidate.id === "string" ? candidate.id : fileName,
    ownerId: numberOrString(candidate.ownerId),
    strategyKey: typeof candidate.strategyKey === "string" ? candidate.strategyKey : null,
    name: typeof candidate.name === "string" ? candidate.name : null,
    description: typeof candidate.description === "string" ? candidate.description : null,
    strategyType: typeof candidate.strategyType === "string" ? candidate.strategyType : null,
    lifecycleStatus:
      typeof candidate.lifecycleStatus === "string" ? candidate.lifecycleStatus : "DRAFT",
    latestVersion:
      typeof candidate.latestVersion === "string" ? candidate.latestVersion : null,
    latestVersionId: numberOrString(candidate.latestVersionId),
    fileName,
    status: typeof candidate.status === "string" ? candidate.status : "INVALID",
    validationError:
      typeof candidate.validationError === "string" ? candidate.validationError : null,
    parametersSchema: objectOrNull(candidate.parametersSchema),
    metadata: objectOrNull(candidate.metadata),
    tags: stringList(candidate.tags),
    contentType: typeof candidate.contentType === "string" ? candidate.contentType : null,
    sizeBytes: typeof candidate.sizeBytes === "number" ? candidate.sizeBytes : null,
    checksum: typeof candidate.checksum === "string" ? candidate.checksum : null,
    uploadedAt: typeof candidate.uploadedAt === "string" ? candidate.uploadedAt : null,
    createdAt: typeof candidate.createdAt === "string" ? candidate.createdAt : "",
    updatedAt: typeof candidate.updatedAt === "string" ? candidate.updatedAt : null,
  };
}

function toStrategyVersion(payload: unknown): StrategyVersion | null {
  if (!payload || typeof payload !== "object") {
    return null;
  }

  const candidate = payload as Record<string, unknown>;
  const id = numberOrString(candidate.id);
  const strategyId = numberOrString(candidate.strategyId);
  if (!id || !strategyId || typeof candidate.version !== "string") {
    return null;
  }

  return {
    id,
    strategyId,
    version: candidate.version,
    filePath: typeof candidate.filePath === "string" ? candidate.filePath : "",
    fileName: typeof candidate.fileName === "string" ? candidate.fileName : "",
    contentType: typeof candidate.contentType === "string" ? candidate.contentType : null,
    sizeBytes: typeof candidate.sizeBytes === "number" ? candidate.sizeBytes : null,
    checksum: typeof candidate.checksum === "string" ? candidate.checksum : "",
    validationStatus:
      typeof candidate.validationStatus === "string" ? candidate.validationStatus : "PENDING",
    validationReport: objectOrNull(candidate.validationReport),
    parametersSchema: objectOrNull(candidate.parametersSchema),
    metadata: objectOrNull(candidate.metadata),
    executionEngineVersion:
      typeof candidate.executionEngineVersion === "string"
        ? candidate.executionEngineVersion
        : null,
    createdAt: typeof candidate.createdAt === "string" ? candidate.createdAt : "",
    createdBy: numberOrString(candidate.createdBy),
  };
}

function toStrategyTemplate(payload: unknown): StrategyTemplate | null {
  if (!payload || typeof payload !== "object") {
    return null;
  }

  const candidate = payload as Record<string, unknown>;
  const id = numberOrString(candidate.id);
  if (!id || typeof candidate.templateKey !== "string" || typeof candidate.name !== "string") {
    return null;
  }

  return {
    id,
    templateKey: candidate.templateKey,
    name: candidate.name,
    description: typeof candidate.description === "string" ? candidate.description : null,
    strategyType: typeof candidate.strategyType === "string" ? candidate.strategyType : null,
    category: typeof candidate.category === "string" ? candidate.category : null,
    defaultParameters: objectOrNull(candidate.defaultParameters) ?? {},
    templateReference:
      typeof candidate.templateReference === "string" ? candidate.templateReference : "",
    metadata: objectOrNull(candidate.metadata),
    createdAt: typeof candidate.createdAt === "string" ? candidate.createdAt : "",
  };
}

function toStrategyPreset(payload: unknown): StrategyPreset | null {
  if (!payload || typeof payload !== "object") {
    return null;
  }

  const candidate = payload as Record<string, unknown>;
  const id = numberOrString(candidate.id);
  const strategyId = numberOrString(candidate.strategyId);
  const userId = numberOrString(candidate.userId);
  if (!id || !strategyId || !userId || typeof candidate.name !== "string") {
    return null;
  }

  return {
    id,
    strategyId,
    userId,
    name: candidate.name,
    presetPayload: objectOrNull(candidate.presetPayload) ?? {},
    createdAt: typeof candidate.createdAt === "string" ? candidate.createdAt : "",
    updatedAt: typeof candidate.updatedAt === "string" ? candidate.updatedAt : "",
  };
}

export async function fetchStrategies() {
  const response = await apiFetch("/api/strategies", {
    method: "GET",
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response));
  }

  const payload = (await response.json()) as unknown;
  return toStrategies(payload);
}

export async function fetchStrategyById(id: number | string) {
  const response = await apiFetch(`/api/strategies/${id}`, {
    method: "GET",
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response));
  }

  const payload = (await response.json()) as unknown;
  const strategy = toStrategy(payload);
  if (!strategy) {
    throw new Error("Invalid strategy response");
  }

  return strategy;
}

export async function uploadStrategy(file: File) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await apiFetch("/api/strategies/upload", {
    method: "POST",
    body: formData,
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response));
  }

  const payload = (await response.json()) as unknown;
  const strategy = toStrategy(payload);
  if (!strategy) {
    throw new Error("Invalid strategy upload response");
  }

  return strategy;
}

export async function fetchStrategyVersions(strategyId: number | string) {
  const response = await apiFetch(`/api/strategies/${strategyId}/versions`, {
    method: "GET",
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response));
  }

  const payload = (await response.json()) as unknown;
  return Array.isArray(payload)
    ? payload
        .map((item) => toStrategyVersion(item))
        .filter((item): item is StrategyVersion => item !== null)
    : [];
}

export async function uploadStrategyVersion(strategyId: number | string, file: File) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await apiFetch(`/api/strategies/${strategyId}/versions`, {
    method: "POST",
    body: formData,
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response));
  }

  const version = toStrategyVersion((await response.json()) as unknown);
  if (!version) {
    throw new Error("Invalid strategy version response");
  }
  return version;
}

export async function validateStrategyVersion(versionId: number | string) {
  const response = await apiFetch(`/api/strategy-versions/${versionId}/validate`, {
    method: "POST",
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response));
  }

  const version = toStrategyVersion((await response.json()) as unknown);
  if (!version) {
    throw new Error("Invalid strategy version response");
  }
  return version;
}

export async function activateStrategyVersion(versionId: number | string) {
  const response = await apiFetch(`/api/strategy-versions/${versionId}/activate`, {
    method: "POST",
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response));
  }

  const version = toStrategyVersion((await response.json()) as unknown);
  if (!version) {
    throw new Error("Invalid strategy version response");
  }
  return version;
}

export async function fetchStrategyTemplates() {
  const response = await apiFetch("/api/strategy-templates", {
    method: "GET",
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response));
  }

  const payload = (await response.json()) as unknown;
  return Array.isArray(payload)
    ? payload
        .map((item) => toStrategyTemplate(item))
        .filter((item): item is StrategyTemplate => item !== null)
    : [];
}

export async function fetchStrategyPresets(strategyId: number | string) {
  const response = await apiFetch(`/api/strategies/${strategyId}/presets`, {
    method: "GET",
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response));
  }

  const payload = (await response.json()) as unknown;
  return Array.isArray(payload)
    ? payload
        .map((item) => toStrategyPreset(item))
        .filter((item): item is StrategyPreset => item !== null)
    : [];
}

export async function createStrategyPreset(
  strategyId: number | string,
  payload: { name: string; presetPayload: Record<string, unknown> }
) {
  const response = await apiFetch(`/api/strategies/${strategyId}/presets`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response));
  }

  const preset = toStrategyPreset((await response.json()) as unknown);
  if (!preset) {
    throw new Error("Invalid strategy preset response");
  }
  return preset;
}
