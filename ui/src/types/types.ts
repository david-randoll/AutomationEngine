// Lightweight types for the fetched schema
type JsonSchema = any;

type Area = "variable" | "trigger" | "condition" | "action" | "result";

type ModuleType = {
    id?: string;
    name?: string;
    label?: string;
    description?: string;
    schema?: JsonSchema;

    // allow additional fields
    [key: string]: unknown;
};
type EditMode = "json" | "yaml" | "ui";

type Path = (string | number)[];
