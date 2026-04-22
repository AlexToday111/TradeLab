import { NextRequest, NextResponse } from "next/server";
import { proxyToBackend } from "@/lib/server/backend-proxy";

export async function POST(request: NextRequest) {
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
    path: "/api/strategies/upload",
    method: "POST",
    headers: {
      Accept: "application/json",
    },
    body: formData,
    errorMessage: "Failed to reach backend /api/strategies/upload",
  });
}
