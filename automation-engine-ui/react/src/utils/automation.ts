import yaml from "js-yaml";

/**
 * Utility to export JSON string from form values
 */
export function exportJson(data: unknown): string {
  try {
    return JSON.stringify(data, null, 2);
  } catch (e) {
    return `Error serializing JSON: ${(e as Error).message}`;
  }
}

/**
 * Utility to export YAML string from form values
 */
export function exportYaml(data: unknown): string {
  try {
    return yaml.dump(data, { noRefs: true });
  } catch (e) {
    return `Error serializing YAML: ${(e as Error).message}`;
  }
}
