import type { Metadata } from "next";
import { Sora } from "next/font/google";
import "./globals.css";
import { AppShell } from "@/components/shell/app-shell";
import { Providers } from "@/app/providers";

const sora = Sora({
  subsets: ["latin"],
  variable: "--font-sans",
  display: "swap",
  weight: ["400", "500", "600", "700"],
});

export const metadata: Metadata = {
  title: "Trade360Lab",
  description: "Интерфейс исследования, запуска и сравнения торговых сценариев.",
  icons: {
    icon: "/favicon.ico",
    shortcut: "/favicon.ico",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ru" data-theme="neon" suppressHydrationWarning>
      <body className={`${sora.variable} bg-background text-foreground`}>
        <Providers>
          <AppShell>{children}</AppShell>
        </Providers>
      </body>
    </html>
  );
}
