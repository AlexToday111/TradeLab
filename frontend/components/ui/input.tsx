import * as React from "react"

import { cn } from "@/lib/utils"

const Input = React.forwardRef<HTMLInputElement, React.ComponentProps<"input">>(
  ({ className, type, ...props }, ref) => {
    return (
      <input
        type={type}
        className={cn(
          "flex h-9 w-full rounded-xl border border-input/80 bg-[linear-gradient(160deg,rgba(45,31,73,0.52),rgba(16,12,30,0.52))] px-3 py-1 text-base text-foreground shadow-[inset_0_1px_0_rgba(255,255,255,0.06)] transition-all duration-300 file:border-0 file:bg-transparent file:text-sm file:font-medium file:text-foreground placeholder:text-muted-foreground/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/80 focus-visible:ring-offset-1 focus-visible:ring-offset-background focus-visible:shadow-[0_0_16px_rgba(153,96,255,0.35)] disabled:cursor-not-allowed disabled:opacity-50 md:text-sm",
          className
        )}
        ref={ref}
        {...props}
      />
    )
  }
)
Input.displayName = "Input"

export { Input }
