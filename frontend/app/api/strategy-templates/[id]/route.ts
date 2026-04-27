import { NextRequest } from "next/server";
import { proxyToBackend } from "@/lib/server/backend-proxy";

export async function GET(
  request: NextRequest,
  context: { params: Promise<{ id: string }> }
) {
  const { id } = await context.params;
  return proxyToBackend({
    request,
    path: `/api/strategy-templates/${id}`,
    headers: {
      Accept: "application/json",
    },
    errorMessage: `Failed to reach backend /api/strategy-templates/${id}`,
  });
}
