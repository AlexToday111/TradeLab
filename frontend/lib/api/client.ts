"use client";

import {
  clearAuthSession,
  getAuthToken,
  redirectToLogin,
} from "@/features/auth/auth-storage";

export async function apiFetch(input: RequestInfo | URL, init?: RequestInit) {
  const headers = new Headers(init?.headers);
  const token = getAuthToken();

  if (token && !headers.has("Authorization")) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(input, {
    ...init,
    headers,
  });

  if (response.status === 401) {
    clearAuthSession();
    redirectToLogin();
  }

  return response;
}
