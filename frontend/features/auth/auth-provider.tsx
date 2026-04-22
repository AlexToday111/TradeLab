"use client";

import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import {
  AUTH_CHANGED_EVENT,
  type AuthSession,
  clearAuthSession,
  readAuthSession,
  saveAuthSession,
} from "@/features/auth/auth-storage";

type AuthContextValue = {
  isReady: boolean;
  isAuthenticated: boolean;
  session: AuthSession | null;
  login: (session: AuthSession) => void;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(null);
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    const syncSession = () => {
      setSession(readAuthSession());
      setIsReady(true);
    };

    syncSession();
    window.addEventListener("storage", syncSession);
    window.addEventListener(AUTH_CHANGED_EVENT, syncSession);

    return () => {
      window.removeEventListener("storage", syncSession);
      window.removeEventListener(AUTH_CHANGED_EVENT, syncSession);
    };
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      isReady,
      isAuthenticated: session !== null,
      session,
      login(nextSession) {
        saveAuthSession(nextSession);
        setSession(nextSession);
      },
      logout() {
        clearAuthSession();
        setSession(null);
      },
    }),
    [isReady, session]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
