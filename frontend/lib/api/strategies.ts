import type { Strategy } from "@/lib/types";
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
    name: typeof candidate.name === "string" ? candidate.name : null,
    fileName,
    status: typeof candidate.status === "string" ? candidate.status : "INVALID",
    validationError:
      typeof candidate.validationError === "string" ? candidate.validationError : null,
    parametersSchema:
      candidate.parametersSchema && typeof candidate.parametersSchema === "object"
        ? (candidate.parametersSchema as Record<string, unknown>)
        : null,
    createdAt: typeof candidate.createdAt === "string" ? candidate.createdAt : "",
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
