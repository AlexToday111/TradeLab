import { NextRequest } from "next/server";
import { proxyToBackend } from "@/lib/server/backend-proxy";

export async function PATCH(
  request: NextRequest,
  context: { params: Promise<{ presetId: string }> }
) {
  const { presetId } = await context.params;
  return proxyToBackend({
    request,
    path: `/api/strategy-presets/${presetId}`,
    method: "PATCH",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    body: await request.text(),
    errorMessage: `Failed to reach backend /api/strategy-presets/${presetId}`,
  });
}

export async function DELETE(
  request: NextRequest,
  context: { params: Promise<{ presetId: string }> }
) {
  const { presetId } = await context.params;
  return proxyToBackend({
    request,
    path: `/api/strategy-presets/${presetId}`,
    method: "DELETE",
    headers: {
      Accept: "application/json",
    },
    errorMessage: `Failed to reach backend /api/strategy-presets/${presetId}`,
  });
}
