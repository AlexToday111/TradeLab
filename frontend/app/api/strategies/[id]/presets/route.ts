import { NextRequest } from "next/server";
import { proxyToBackend } from "@/lib/server/backend-proxy";

export async function GET(
  request: NextRequest,
  context: { params: Promise<{ id: string }> }
) {
  const { id } = await context.params;
  return proxyToBackend({
    request,
    path: `/api/strategies/${id}/presets`,
    headers: {
      Accept: "application/json",
    },
    errorMessage: `Failed to reach backend /api/strategies/${id}/presets`,
  });
}

export async function POST(
  request: NextRequest,
  context: { params: Promise<{ id: string }> }
) {
  const { id } = await context.params;
  return proxyToBackend({
    request,
    path: `/api/strategies/${id}/presets`,
    method: "POST",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    body: await request.text(),
    errorMessage: `Failed to reach backend /api/strategies/${id}/presets`,
  });
}
