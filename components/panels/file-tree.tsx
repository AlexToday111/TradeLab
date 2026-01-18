"use client";

import { File, Folder, ChevronRight } from "lucide-react";
import { FileNode } from "@/lib/mock-data/files";

function TreeNode({ node, depth = 0 }: { node: FileNode; depth?: number }) {
  return (
    <div className="flex flex-col gap-1">
      <div
        className="flex items-center gap-2 text-xs text-muted-foreground"
        style={{ paddingLeft: `${depth * 12}px` }}
      >
        {node.type === "folder" ? (
          <>
            <ChevronRight className="h-3 w-3 text-muted-foreground" />
            <Folder className="h-4 w-4 text-muted-foreground" />
          </>
        ) : (
          <File className="h-4 w-4 text-muted-foreground" />
        )}
        <span className="text-foreground">{node.name}</span>
      </div>
      {node.children?.map((child) => (
        <TreeNode key={`${node.name}-${child.name}`} node={child} depth={depth + 1} />
      ))}
    </div>
  );
}

export function FileTree({ nodes }: { nodes: FileNode[] }) {
  return (
    <div className="flex flex-col gap-2">
      {nodes.map((node) => (
        <TreeNode key={node.name} node={node} />
      ))}
    </div>
  );
}
