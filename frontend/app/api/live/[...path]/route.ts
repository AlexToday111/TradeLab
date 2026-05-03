import { NextRequest } from "next/server";
import { proxyToBackend } from "@/lib/server/backend-proxy";

type RouteContext = {
  params: Promise<{ path: string[] }>;
};

async function proxyLive(request: NextRequest, context: RouteContext) {
  const { path } = await context.params;
  const suffix = path.join("/");
  const search = request.nextUrl.search;
  return proxyToBackend({
    request,
    path: `/api/live/${suffix}${search}`,
    method: request.method,
    body: request.body,
    errorMessage: "Live trading API request failed",
  });
}

export async function GET(request: NextRequest, context: RouteContext) {
  return proxyLive(request, context);
}

export async function POST(request: NextRequest, context: RouteContext) {
  return proxyLive(request, context);
}
