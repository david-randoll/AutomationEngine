// Lightweight types for the fetched schema
type JsonSchema = any;

type ModuleType = {
    id?: string;
    name: string;
    label?: string;
    description?: string;
    schema?: JsonSchema;
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
