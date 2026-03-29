import type { Strategy } from "@/lib/types";

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
    return payload as Strategy[];
  }

  return [];
}

export async function fetchStrategies() {
  const response = await fetch("/api/strategies", {
    method: "GET",
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response));
  }

  const payload = (await response.json()) as unknown;
  return toStrategies(payload);
}

export async function uploadStrategy(file: File) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await fetch("/api/strategies/upload", {
    method: "POST",
    body: formData,
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response));
  }

  return (await response.json()) as Strategy;
}
