"use client";

import { useEffect, useRef } from "react";
import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { projects } from "@/lib/mock-data/projects";

export function Topbar() {
  const searchRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "p") {
        event.preventDefault();
        searchRef.current?.focus();
      }
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, []);

  return (
    <header className="flex h-14 items-center justify-between border-b border-border bg-panel/80 px-4">
      <div className="flex items-center gap-4">
        <div className="flex items-center gap-2 text-xs text-muted-foreground">
          <span>Проект</span>
          <Select defaultValue={projects[0]?.id}>
            <SelectTrigger className="h-8 w-[200px] border-border bg-panel-subtle text-xs">
              <SelectValue placeholder="Выберите проект" />
            </SelectTrigger>
            <SelectContent>
              {projects.map((project) => (
                <SelectItem key={project.id} value={project.id}>
                  {project.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="relative w-[320px]">
          <Search className="absolute left-2 top-2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Поиск (Cmd/Ctrl+P)"
            className="h-8 pl-8 text-xs"
            ref={searchRef}
          />
        </div>
      </div>
      <div className="flex items-center gap-3">
        <Badge className="border border-status-running/40 bg-status-running/20 text-status-running">
          локально
        </Badge>
      </div>
    </header>
  );
}
