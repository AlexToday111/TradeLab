import { NextRequest, NextResponse } from "next/server";
import { proxyToBackend } from "@/lib/server/backend-proxy";

type RouteContext = {
  params: Promise<{ path: string[] }>;
};

function buildPaperPath(path: string[]) {
  return `/api/paper/${path.map(encodeURIComponent).join("/")}`;
}

export async function GET(request: NextRequest, context: RouteContext) {
  const { path } = await context.params;
  const paperPath = buildPaperPath(path);
  return proxyToBackend({
    request,
    path: paperPath,
    headers: {
      Accept: "application/json",
    },
    errorMessage: `Failed to reach backend ${paperPath}`,
  });
}

export async function POST(request: NextRequest, context: RouteContext) {
  let body: unknown = null;

  try {
    body = await request.json();
  } catch {
    body = null;
  }

  const { path } = await context.params;
  const paperPath = buildPaperPath(path);
  return proxyToBackend({
    request,
    path: paperPath,
    method: "POST",
    headers: {
      "content-type": "application/json",
      Accept: "application/json",
    },
    body: body == null ? null : JSON.stringify(body),
    errorMessage: `Failed to reach backend ${paperPath}`,
  });
}
