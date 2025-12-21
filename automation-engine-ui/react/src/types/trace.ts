/**
 * TypeScript types for automation execution tracing.
 * These types match the Java classes in automation-engine-tracing module.
 */

/**
 * Snapshot of state at a point in time during execution.
 */
export interface TraceSnapshot {
  /** Snapshot of the event data */
  eventSnapshot?: Record<string, unknown>;
  /** Snapshot of the context data */
  contextSnapshot?: Record<string, unknown>;
}

/**
 * Nested trace entries for child operations.
 * Used when actions contain nested conditions, actions, etc. (e.g., ifThenElse action).
 */
export interface TraceChildren {
  variables?: VariableTraceEntry[];
  triggers?: TriggerTraceEntry[];
  conditions?: ConditionTraceEntry[];
  actions?: ActionTraceEntry[];
  result?: ResultTraceEntry;
}

/**
 * Base interface for all trace entries.
 */
export interface BaseTraceEntry {
  /** The type of the component (e.g., "loggerAction", "expression", "alwaysTrueTrigger") */
  type?: string;
  /** The alias of the component, if specified */
  alias?: string;
  /** Timestamp when execution started (milliseconds since epoch) */
  startedAt?: number;
  /** Timestamp when execution finished (milliseconds since epoch) */
  finishedAt?: number;
  /** Snapshot of state before execution */
  before?: TraceSnapshot;
  /** Snapshot of state after execution */
  after?: TraceSnapshot;
  /** Nested children entries for composite operations */
  children?: TraceChildren;
}

/**
 * Trace entry for variable resolution operations.
 */
export interface VariableTraceEntry extends BaseTraceEntry {
  // Inherits all from BaseTraceEntry
}

/**
 * Trace entry for trigger evaluation operations.
 */
export interface TriggerTraceEntry extends BaseTraceEntry {
  /** Whether the trigger was activated (returned true) */
  activated?: boolean;
}

/**
 * Trace entry for condition evaluation operations.
 */
export interface ConditionTraceEntry extends BaseTraceEntry {
  /** Whether the condition was satisfied (returned true) */
  satisfied?: boolean;
}

/**
 * Trace entry for action execution operations.
 */
export interface ActionTraceEntry extends BaseTraceEntry {
  // Inherits all from BaseTraceEntry
}

/**
 * Trace entry for result computation operations.
 */
export interface ResultTraceEntry extends BaseTraceEntry {
  /** The computed result value */
  result?: unknown;
}

/**
 * Container for all trace entries in an execution.
 */
export interface TraceData {
  variables?: VariableTraceEntry[];
  triggers?: TriggerTraceEntry[];
  conditions?: ConditionTraceEntry[];
  actions?: ActionTraceEntry[];
  result?: ResultTraceEntry;
}

/**
 * Complete execution trace for an automation run.
 */
export interface ExecutionTrace {
  /** Unique identifier for this execution */
  executionId?: string;
  /** The alias of the automation that was executed */
  alias?: string;
  /** Timestamp when the automation execution started (milliseconds since epoch) */
  startedAt?: number;
  /** Timestamp when the automation execution finished (milliseconds since epoch) */
  finishedAt?: number;
  /** The trace data containing all component executions */
  trace?: TraceData;
}

/**
 * Automation format types.
 */
export type AutomationFormat = "YAML" | "JSON";

/**
 * Request to execute an automation in the playground.
 */
export interface ExecuteAutomationRequest {
  /** The automation definition as a string (YAML or JSON) */
  automation: string;
  /** The format of the automation definition */
  format?: AutomationFormat;
  /** Input data to be used as the event for the automation */
  inputs?: Record<string, unknown>;
}

/**
 * Response from automation execution in the playground.
 */
export interface ExecuteAutomationResponse {
  /** Whether the automation was executed (all conditions met) */
  executed: boolean;
  /** The result value from the automation execution, if any */
  result?: unknown;
  /** The execution trace containing detailed timing and state information */
  trace?: ExecutionTrace;
  /** Error message if execution failed */
  error?: string;
}

/**
 * Trace entry category for color coding and grouping.
 */
export type TraceEntryCategory = "variable" | "trigger" | "condition" | "action" | "result";

/**
 * Union type of all trace entry types with category.
 */
export type TraceEntry = (
  | (VariableTraceEntry & { category: "variable" })
  | (TriggerTraceEntry & { category: "trigger" })
  | (ConditionTraceEntry & { category: "condition" })
  | (ActionTraceEntry & { category: "action" })
  | (ResultTraceEntry & { category: "result" })
) & {
  /** Unique ID for React Flow nodes */
  id: string;
};
