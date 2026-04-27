import { NextRequest } from "next/server";
import { proxyToBackend } from "@/lib/server/backend-proxy";

export async function POST(
  request: NextRequest,
  context: { params: Promise<{ versionId: string }> }
) {
  const { versionId } = await context.params;
  return proxyToBackend({
    request,
    path: `/api/strategy-versions/${versionId}/validate`,
    method: "POST",
    headers: {
      Accept: "application/json",
    },
    errorMessage: `Failed to reach backend /api/strategy-versions/${versionId}/validate`,
  });
}
