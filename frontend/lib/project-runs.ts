import type { Run } from "@/lib/types";

const projectRunKeywords: Record<string, string[]> = {
  "proj-atlas": ["atlas"],
  "proj-orbit": ["orbit"],
  "proj-ridge": ["ridge"],
};
const PROJECT_TAG_PREFIX = "project:";

function strategyToKey(strategy: string) {
  return strategy.toLowerCase();
}

export function getRunProjectId(run: Pick<Run, "strategy"> & Partial<Pick<Run, "tags">>) {
  const projectTag = run.tags?.find((tag) => tag.startsWith(PROJECT_TAG_PREFIX));
  if (projectTag) {
    return projectTag.slice(PROJECT_TAG_PREFIX.length);
  }

  const strategyKey = strategyToKey(run.strategy);

  for (const [projectId, keywords] of Object.entries(projectRunKeywords)) {
    if (keywords.some((keyword) => strategyKey.includes(keyword))) {
      return projectId;
    }
  }

  return null;
}

export function getProjectRuns(runs: Run[], projectId: string) {
  return runs.filter((run) => getRunProjectId(run) === projectId);
}
