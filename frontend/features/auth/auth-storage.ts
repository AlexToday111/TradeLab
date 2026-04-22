"use client";

export const AUTH_STORAGE_KEY = "tradelab.auth";
export const AUTH_CHANGED_EVENT = "tradelab-auth-changed";

export type AuthSession = {
  token: string;
  user: {
    id: number;
    email: string;
    createdAt: string;
  };
};

function isBrowser() {
  return typeof window !== "undefined";
}

export function readAuthSession(): AuthSession | null {
  if (!isBrowser()) {
    return null;
  }

  const raw = window.localStorage.getItem(AUTH_STORAGE_KEY);
  if (!raw) {
    return null;
  }

  try {
    const parsed = JSON.parse(raw) as AuthSession;
    if (!parsed?.token || !parsed?.user?.email) {
      return null;
    }
    return parsed;
  } catch {
    return null;
  }
}

export function getAuthToken() {
  return readAuthSession()?.token ?? null;
}

function notifyAuthChanged() {
  if (!isBrowser()) {
    return;
  }
  window.dispatchEvent(new Event(AUTH_CHANGED_EVENT));
}

export function saveAuthSession(session: AuthSession) {
  if (!isBrowser()) {
    return;
  }
  window.localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session));
  notifyAuthChanged();
}

export function clearAuthSession() {
  if (!isBrowser()) {
    return;
  }
  window.localStorage.removeItem(AUTH_STORAGE_KEY);
  notifyAuthChanged();
}

export function redirectToLogin() {
  if (!isBrowser()) {
    return;
  }
  const { pathname } = window.location;
  if (pathname === "/login" || pathname === "/register") {
    return;
  }
  window.location.assign("/login");
}
