import * as React from "react"
import { cva, type VariantProps } from "class-variance-authority"

import { cn } from "@/lib/utils"

const badgeVariants = cva(
  "inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold tracking-[0.02em] transition-all duration-300 focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2",
  {
    variants: {
      variant: {
        default:
          "border-primary/35 bg-[linear-gradient(130deg,rgba(157,95,255,0.35),rgba(95,56,188,0.2))] text-primary-foreground shadow-[0_0_14px_rgba(150,90,255,0.32)] hover:shadow-[0_0_20px_rgba(162,104,255,0.44)]",
        secondary:
          "border-border/70 bg-[linear-gradient(130deg,rgba(90,60,145,0.28),rgba(45,30,78,0.18))] text-secondary-foreground",
        destructive:
          "border-destructive/50 bg-destructive/20 text-destructive-foreground",
        outline: "border-border/80 text-foreground",
      },
    },
    defaultVariants: {
      variant: "default",
    },
  }
)

export interface BadgeProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof badgeVariants> {}

function Badge({ className, variant, ...props }: BadgeProps) {
  return (
    <div className={cn(badgeVariants({ variant }), className)} {...props} />
  )
}

export { Badge, badgeVariants }
