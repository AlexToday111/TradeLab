export type StrategyStatus = "PENDING" | "VALID" | "INVALID" | string;
export type StrategyLifecycleStatus = "DRAFT" | "ACTIVE" | "DEPRECATED" | "ARCHIVED" | string;
export type StrategyValidationStatus = "PENDING" | "VALID" | "WARNING" | "INVALID" | string;

export type Strategy = {
  id: number | string;
  ownerId: number | string | null;
  strategyKey: string | null;
  name: string | null;
  description: string | null;
  strategyType: string | null;
  lifecycleStatus: StrategyLifecycleStatus;
  latestVersion: string | null;
  latestVersionId: number | string | null;
  fileName: string;
  status: StrategyStatus;
  validationError: string | null;
  parametersSchema: Record<string, unknown> | null;
  metadata: Record<string, unknown> | null;
  tags: string[];
  contentType: string | null;
  sizeBytes: number | null;
  checksum: string | null;
  uploadedAt: string | null;
  createdAt: string;
  updatedAt: string | null;
};

export type StrategyVersion = {
  id: number | string;
  strategyId: number | string;
  version: string;
  filePath: string;
  fileName: string;
  contentType: string | null;
  sizeBytes: number | null;
  checksum: string;
  validationStatus: StrategyValidationStatus;
  validationReport: Record<string, unknown> | null;
  parametersSchema: Record<string, unknown> | null;
  metadata: Record<string, unknown> | null;
  executionEngineVersion: string | null;
  createdAt: string;
  createdBy: number | string | null;
};

export type StrategyTemplate = {
  id: number | string;
  templateKey: string;
  name: string;
  description: string | null;
  strategyType: string | null;
  category: string | null;
  defaultParameters: Record<string, unknown>;
  templateReference: string;
  metadata: Record<string, unknown> | null;
  createdAt: string;
};

export type StrategyPreset = {
  id: number | string;
  strategyId: number | string;
  userId: number | string;
  name: string;
  presetPayload: Record<string, unknown>;
  createdAt: string;
  updatedAt: string;
};
