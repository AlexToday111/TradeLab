import { NextRequest, NextResponse } from "next/server";
import { proxyToBackend } from "@/lib/server/backend-proxy";

export async function POST(request: NextRequest) {
  let body: unknown;

  try {
    body = await request.json();
  } catch {
    return NextResponse.json({ message: "Invalid JSON body" }, { status: 400 });
  }

  return proxyToBackend({
    request,
    path: "/api/auth/register",
    method: "POST",
    headers: {
      "content-type": "application/json",
      Accept: "application/json",
    },
    body: JSON.stringify(body),
    errorMessage: "Failed to reach backend /api/auth/register",
  });
}
