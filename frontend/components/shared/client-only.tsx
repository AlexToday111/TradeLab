"use client";

import { ReactNode, useSyncExternalStore } from "react";

const subscribe = () => () => {};

export function ClientOnly({
  children,
  fallback = null,
}: {
  children: ReactNode;
  fallback?: ReactNode;
}) {
  const mounted = useSyncExternalStore(subscribe, () => true, () => false);

  if (!mounted) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
}
