import * as React from "react"

import { cn } from "@/lib/utils"

const Input = React.forwardRef<HTMLInputElement, React.ComponentProps<"input">>(
  ({ className, type, ...props }, ref) => {
    return (
      <input
        type={type}
        className={cn(
          "flex h-9 w-full rounded-xl border border-input/80 bg-[linear-gradient(160deg,hsl(var(--tl-bg-1)/0.98),hsl(var(--tl-bg-2)/0.92))] px-3 py-1 text-base text-foreground shadow-[inset_0_1px_0_hsl(var(--tl-glass-highlight)/0.08)] transition-all duration-300 file:border-0 file:bg-transparent file:text-sm file:font-medium file:text-foreground placeholder:text-muted-foreground/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/80 focus-visible:ring-offset-1 focus-visible:ring-offset-background focus-visible:shadow-[0_0_16px_rgba(43,213,118,0.18)] disabled:cursor-not-allowed disabled:opacity-50 md:text-sm",
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
