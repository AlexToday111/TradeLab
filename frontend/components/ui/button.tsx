import * as React from "react"
import { Slot } from "@radix-ui/react-slot"
import { cva, type VariantProps } from "class-variance-authority"

import { cn } from "@/lib/utils"

const buttonVariants = cva(
  "inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-[18px] text-sm font-medium transition-all duration-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/70 focus-visible:ring-offset-2 focus-visible:ring-offset-background disabled:pointer-events-none disabled:opacity-50 disabled:shadow-none [&_svg]:pointer-events-none [&_svg]:size-4 [&_svg]:shrink-0",
  {
    variants: {
      variant: {
        default:
          "border border-white/15 bg-[linear-gradient(135deg,#2BD576_0%,#6FF7A3_100%)] text-[#07110b] shadow-[inset_0_1px_0_rgba(255,255,255,0.16),0_14px_30px_rgba(43,213,118,0.24)] hover:-translate-y-0.5 hover:brightness-110 hover:shadow-[0_0_26px_rgba(43,213,118,0.28)]",
        destructive:
          "border border-destructive/50 bg-destructive text-destructive-foreground shadow-[0_10px_24px_rgba(191,63,83,0.35)] hover:-translate-y-0.5 hover:bg-destructive/90",
        outline:
          "border border-white/10 bg-[linear-gradient(145deg,rgba(20,25,39,0.86),rgba(12,16,27,0.92))] text-foreground shadow-[inset_0_1px_0_rgba(255,255,255,0.06)] hover:border-[rgba(92,240,158,0.24)] hover:text-foreground hover:shadow-[0_0_18px_rgba(43,213,118,0.16)]",
        secondary:
          "border border-white/10 bg-[linear-gradient(145deg,rgba(43,213,118,0.12),rgba(111,247,163,0.08))] text-secondary-foreground hover:border-[rgba(92,240,158,0.24)] hover:shadow-[0_0_20px_rgba(43,213,118,0.14)]",
        ghost:
          "text-muted-foreground hover:bg-[linear-gradient(135deg,rgba(43,213,118,0.12),rgba(111,247,163,0.08))] hover:text-foreground",
        link: "text-primary underline-offset-4 hover:underline",
      },
      size: {
        default: "h-9 px-4 py-2",
        sm: "h-8 rounded-full px-3 text-xs",
        lg: "h-10 rounded-full px-8",
        icon: "h-9 w-9",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
)

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : "button"
    return (
      <Comp
        className={cn(buttonVariants({ variant, size, className }))}
        ref={ref}
        {...props}
      />
    )
  }
)
Button.displayName = "Button"

export { Button, buttonVariants }
