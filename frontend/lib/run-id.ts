const RUN_ID_PATTERN = /^run_(\d+)$/;

function getMaxRunIdNumber(existingRunIds: string[]) {
  return existingRunIds.reduce((max, runId) => {
    const match = RUN_ID_PATTERN.exec(runId);
    if (!match) {
      return max;
    }

    const parsed = Number.parseInt(match[1], 10);
    if (Number.isNaN(parsed)) {
      return max;
    }

    return Math.max(max, parsed);
  }, 0);
}

export function getNextRunId(existingRunIds: string[]) {
  return `run_${getMaxRunIdNumber(existingRunIds) + 1}`;
}

export function getNextRunIds(existingRunIds: string[], count: number) {
  if (count <= 0) {
    return [];
  }

  const start = getMaxRunIdNumber(existingRunIds) + 1;
  return Array.from({ length: count }, (_, index) => `run_${start + index}`);
}
