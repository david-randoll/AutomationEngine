// Lightweight types for the fetched schema
type JsonSchema = any;

type Area = "variable" | "trigger" | "condition" | "action" | "result";
type AreaPlural = "variables" | "triggers" | "conditions" | "actions" | "results";

type PathSegment = string | number;
type Path = PathSegment[];

type ModuleType = {
    id: string;
    name: string;
    label?: string;
    description?: string;
    schema?: JsonSchema;
    data?: Record<string, any>;
};

type Automation = {
    alias?: string;
    description?: string;
    variables: ModuleType[];
    triggers: ModuleType[];
    conditions: ModuleType[];
    actions: ModuleType[];
    results: ModuleType[];
};
