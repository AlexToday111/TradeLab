"use client";

import Image from "next/image";
import Link from "next/link";

export function LogoPlaceholder() {
  return (
    <Link
      href="/workspace"
      className="block w-[120px] p-0"
      aria-label="Открыть главное"
    >
      <div className="overflow-hidden">
        <Image
          src="/logo.png"
          alt="Логотип TradeLab"
          width={120}
          height={120}
          className="h-auto w-full object-contain"
          priority
        />
      </div>
    </Link>
  );
}
