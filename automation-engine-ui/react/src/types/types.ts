// Lightweight types for the fetched schema
export type JsonSchema = Record<string, unknown>;

export type Area = "variable" | "trigger" | "condition" | "action" | "result";

export type ModuleType = {
  name?: string;
  label?: string;
  description?: string;
  schema?: JsonSchema;
  examples?: unknown[];

  // allow additional fields
  [key: string]: unknown;
};
export type EditMode = "json" | "yaml" | "ui";

export type Path = (string | number)[];
