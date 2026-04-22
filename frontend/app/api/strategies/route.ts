import { NextRequest } from "next/server";
import { proxyToBackend } from "@/lib/server/backend-proxy";

export async function GET(request: NextRequest) {
  return proxyToBackend({
    request,
    path: "/api/strategies",
    headers: {
      Accept: "application/json",
    },
    errorMessage: "Failed to reach backend /api/strategies",
  });
}
