// Types for user-defined block types

export interface BaseDefinition {
  alias?: string;
  description?: string;
  name: string;
  parameters?: Record<string, unknown>;
}

export interface UserDefinedActionDefinition extends BaseDefinition {
  variables?: unknown[];
  conditions?: unknown[];
  actions?: unknown[];
}

export interface UserDefinedConditionDefinition extends BaseDefinition {
  variables?: unknown[];
  conditions?: unknown[];
}

export interface UserDefinedTriggerDefinition extends BaseDefinition {
  variables?: unknown[];
  triggers?: unknown[];
}

export interface UserDefinedVariableDefinition extends BaseDefinition {
  variables?: unknown[];
}

export type BlockType = "actions" | "conditions" | "triggers" | "variables";

export type UserDefinedDefinition =
  | UserDefinedActionDefinition
  | UserDefinedConditionDefinition
  | UserDefinedTriggerDefinition
  | UserDefinedVariableDefinition;
