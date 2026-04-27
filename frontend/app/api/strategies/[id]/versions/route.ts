import { NextRequest, NextResponse } from "next/server";
import { proxyToBackend } from "@/lib/server/backend-proxy";

export async function GET(
  request: NextRequest,
  context: { params: Promise<{ id: string }> }
) {
  const { id } = await context.params;
  return proxyToBackend({
    request,
    path: `/api/strategies/${id}/versions`,
    headers: {
      Accept: "application/json",
    },
    errorMessage: `Failed to reach backend /api/strategies/${id}/versions`,
  });
}

export async function POST(
  request: NextRequest,
  context: { params: Promise<{ id: string }> }
) {
  const { id } = await context.params;
  let formData: FormData;

  try {
    formData = await request.formData();
  } catch {
    return NextResponse.json({ message: "Invalid multipart form data" }, { status: 400 });
  }

  const file = formData.get("file");
  if (!(file instanceof File)) {
    return NextResponse.json({ message: "Field 'file' is required" }, { status: 400 });
  }

  return proxyToBackend({
    request,
    path: `/api/strategies/${id}/versions`,
    method: "POST",
    headers: {
      Accept: "application/json",
    },
    body: formData,
    errorMessage: `Failed to reach backend /api/strategies/${id}/versions`,
  });
}
