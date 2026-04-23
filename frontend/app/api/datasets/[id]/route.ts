import { NextRequest, NextResponse } from "next/server";
import { proxyToBackend } from "@/lib/server/backend-proxy";

export async function GET(
  request: NextRequest,
  context: { params: Promise<{ id: string }> }
) {
  const { id } = await context.params;
  return proxyToBackend({
    request,
    path: `/api/datasets/${id}`,
    headers: {
      Accept: "application/json",
    },
    errorMessage: `Failed to reach backend /api/datasets/${id}`,
  });
}

export async function PATCH(
  request: NextRequest,
  context: { params: Promise<{ id: string }> }
) {
  let body: unknown;

  try {
    body = await request.json();
  } catch {
    return NextResponse.json({ message: "Invalid JSON body" }, { status: 400 });
  }

  const { id } = await context.params;
  return proxyToBackend({
    request,
    path: `/api/datasets/${id}`,
    method: "PATCH",
    headers: {
      "content-type": "application/json",
      Accept: "application/json",
    },
    body: JSON.stringify(body),
    errorMessage: `Failed to reach backend /api/datasets/${id}`,
  });
}

export async function DELETE(
  request: NextRequest,
  context: { params: Promise<{ id: string }> }
) {
  const { id } = await context.params;
  return proxyToBackend({
    request,
    path: `/api/datasets/${id}`,
    method: "DELETE",
    errorMessage: `Failed to reach backend /api/datasets/${id}`,
  });
}
