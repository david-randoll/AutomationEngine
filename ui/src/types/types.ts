// Lightweight types for the fetched schema
type JsonSchema = any;

type ModuleType = {
    name: string;
    label?: string;
    description?: string;
    schema?: JsonSchema;
};

type Automation = {
    alias?: string;
    description?: string;
    variables: { name: string; value: any }[];
    triggers: ModuleType[];
    conditions: ModuleType[];
    actions: ModuleType[];
    results: ModuleType[];
};