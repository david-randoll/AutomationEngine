import yaml from "js-yaml";

/**
 * Recursively removes any key named "schema" from the given data.
 */
function removeSchema(data: any): any {
    if (Array.isArray(data)) {
        return data.map((item) => removeSchema(item));
    } else if (data && typeof data === "object") {
        const cleaned: any = {};
        for (const [key, value] of Object.entries(data)) {
            if (key === "schema") {
                continue; // exclude schema
            }
            cleaned[key] = removeSchema(value);
        }
        return cleaned;
    }
    return data;
}

/**
 * Utility to export JSON string from form values
 */
export function exportJson(data: any): string {
    try {
        const cleaned = removeSchema(data);
        return JSON.stringify(cleaned, null, 2);
    } catch (e) {
        return `Error serializing JSON: ${(e as Error).message}`;
    }
}

/**
 * Utility to export YAML string from form values
 */
export function exportYaml(data: any): string {
    try {
        const cleaned = removeSchema(data);
        return yaml.dump(cleaned, { noRefs: true });
    } catch (e) {
        return `Error serializing YAML: ${(e as Error).message}`;
    }
}
