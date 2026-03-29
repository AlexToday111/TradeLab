import { NextResponse } from "next/server";

const backendBaseUrl = process.env.BACKEND_API_BASE_URL ?? "http://127.0.0.1:8080";

export async function GET() {
  try {
    const response = await fetch(new URL("/api/strategies", backendBaseUrl), {
      headers: {
        Accept: "application/json",
      },
      cache: "no-store",
    });

    const payload = await response.text();
    return new NextResponse(payload, {
      status: response.status,
      headers: {
        "content-type": response.headers.get("content-type") ?? "application/json",
      },
    });
  } catch (error) {
    return NextResponse.json(
      {
        message:
          error instanceof Error ? error.message : "Failed to reach backend /api/strategies",
      },
      { status: 502 }
    );
  }
}
