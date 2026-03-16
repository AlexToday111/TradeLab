import * as React from "react"
import { Slot } from "@radix-ui/react-slot"
import { cva, type VariantProps } from "class-variance-authority"

import { cn } from "@/lib/utils"

const buttonVariants = cva(
  "inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-xl text-sm font-medium transition-all duration-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/70 focus-visible:ring-offset-2 focus-visible:ring-offset-background disabled:pointer-events-none disabled:opacity-50 disabled:shadow-none [&_svg]:pointer-events-none [&_svg]:size-4 [&_svg]:shrink-0",
  {
    variants: {
      variant: {
        default:
          "border border-primary/45 bg-[linear-gradient(135deg,hsl(var(--primary))_0%,hsl(var(--accent))_100%)] text-primary-foreground shadow-[0_10px_24px_rgba(107,58,216,0.42)] hover:-translate-y-0.5 hover:brightness-110 hover:shadow-[0_0_26px_rgba(154,97,255,0.6)]",
        destructive:
          "border border-destructive/50 bg-destructive text-destructive-foreground shadow-[0_10px_24px_rgba(191,63,83,0.35)] hover:-translate-y-0.5 hover:bg-destructive/90",
        outline:
          "border border-border/80 bg-[linear-gradient(145deg,rgba(41,28,67,0.66),rgba(15,11,29,0.66))] text-foreground shadow-[inset_0_1px_0_rgba(255,255,255,0.06)] hover:border-primary/40 hover:text-foreground hover:shadow-[0_0_18px_rgba(140,88,240,0.33)]",
        secondary:
          "border border-border/70 bg-[linear-gradient(145deg,rgba(72,43,130,0.45),rgba(30,19,58,0.45))] text-secondary-foreground hover:border-primary/45 hover:shadow-[0_0_20px_rgba(140,88,240,0.33)]",
        ghost:
          "text-muted-foreground hover:bg-[linear-gradient(135deg,rgba(121,75,218,0.24),rgba(58,36,109,0.16))] hover:text-foreground",
        link: "text-primary underline-offset-4 hover:underline",
      },
      size: {
        default: "h-9 px-4 py-2",
        sm: "h-8 rounded-lg px-3 text-xs",
        lg: "h-10 rounded-lg px-8",
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
