import { NextRequest, NextResponse } from "next/server";

const backendBaseUrl = process.env.BACKEND_API_BASE_URL ?? "http://127.0.0.1:8080";

type ProxyOptions = {
  request?: NextRequest;
  path: string;
  method?: string;
  body?: BodyInit | null;
  headers?: HeadersInit;
  errorMessage: string;
};

function buildHeaders(request?: NextRequest, headers?: HeadersInit) {
  const nextHeaders = new Headers(headers);
  const authorization = request?.headers.get("authorization");
  if (authorization && !nextHeaders.has("authorization")) {
    nextHeaders.set("authorization", authorization);
  }
  return nextHeaders;
}

function buildResponse(response: Response, payload: string) {
  const headers = new Headers();
  const contentType = response.headers.get("content-type");
  if (contentType) {
    headers.set("content-type", contentType);
  }
  const contentDisposition = response.headers.get("content-disposition");
  if (contentDisposition) {
    headers.set("content-disposition", contentDisposition);
  }
  return new NextResponse(payload, {
    status: response.status,
    headers,
  });
}

export async function proxyToBackend({
  request,
  path,
  method = request?.method ?? "GET",
  body,
  headers,
  errorMessage,
}: ProxyOptions) {
  try {
    const response = await fetch(new URL(path, backendBaseUrl), {
      method,
      headers: buildHeaders(request, headers),
      body,
      cache: "no-store",
    });

    const payload = await response.text();
    return buildResponse(response, payload);
  } catch (error) {
    return NextResponse.json(
      {
        message: error instanceof Error ? error.message : errorMessage,
      },
      { status: 502 }
    );
  }
}
