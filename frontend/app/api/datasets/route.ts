import { NextRequest, NextResponse } from "next/server";
import { proxyToBackend } from "@/lib/server/backend-proxy";

export async function GET(request: NextRequest) {
  return proxyToBackend({
    request,
    path: "/api/datasets",
    headers: {
      Accept: "application/json",
    },
    errorMessage: "Failed to reach backend /api/datasets",
  });
}

export async function POST(request: NextRequest) {
  let body: unknown;

  try {
    body = await request.json();
  } catch {
    return NextResponse.json({ message: "Invalid JSON body" }, { status: 400 });
  }

  return proxyToBackend({
    request,
    path: "/api/datasets",
    method: "POST",
    headers: {
      "content-type": "application/json",
      Accept: "application/json",
    },
    body: JSON.stringify(body),
    errorMessage: "Failed to reach backend /api/datasets",
  });
}
