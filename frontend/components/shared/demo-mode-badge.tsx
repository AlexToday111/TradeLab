import { Badge } from "@/components/ui/badge";

export function DemoModeBadge({ label = "Demo mode" }: { label?: string }) {
  if (process.env.NEXT_PUBLIC_DEMO_MODE === "false") {
    return null;
  }

  return (
    <Badge
      variant="outline"
      data-testid="demo-mode-badge"
      className="rounded-full border-status-warning/35 bg-status-warning/12 text-status-warning"
    >
      {label}
    </Badge>
  );
}
