// Lightweight types for the fetched schema
type JsonSchema = any;

type Area = "variable" | "trigger" | "condition" | "action" | "result";
type AreaPlural = "variables" | "triggers" | "conditions" | "actions" | "results";

type ModuleType = {
    id: string;
    name: string;
    label?: string;
    description?: string;
    schema?: JsonSchema;
};
type EditMode = "json" | "yaml" | "ui";
