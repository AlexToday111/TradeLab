import { proxyToBackend } from "@/lib/server/backend-proxy";

export async function GET() {
  return proxyToBackend({
    path: "/api/health",
    errorMessage: "Java API health check failed",
  });
}
