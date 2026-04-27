import { NextRequest } from "next/server";
import { proxyToBackend } from "@/lib/server/backend-proxy";

export async function POST(
  request: NextRequest,
  context: { params: Promise<{ id: string }> }
) {
  const { id } = await context.params;
  return proxyToBackend({
    request,
    path: `/api/strategies/${id}/archive`,
    method: "POST",
    headers: {
      Accept: "application/json",
    },
    errorMessage: `Failed to reach backend /api/strategies/${id}/archive`,
  });
}
