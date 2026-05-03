import { proxyToBackend } from "@/lib/server/backend-proxy";

export async function GET() {
  return proxyToBackend({
    path: "/api/python/health",
    errorMessage: "Python parser health check failed",
  });
}
