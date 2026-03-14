import type { Metadata } from "next";
import "./globals.css";
import { AppShell } from "@/components/shell/app-shell";
import { Providers } from "@/app/providers";

export const metadata: Metadata = {
  title: "TradeLab",
  description: "Интерфейс исследования, запуска и сравнения торговых сценариев.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ru" className="dark">
      <body className="bg-background text-foreground">
        <Providers>
          <AppShell>{children}</AppShell>
        </Providers>
      </body>
    </html>
  );
}
