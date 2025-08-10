import yaml from 'js-yaml';

export function simplifyModules(modules: ModuleType[], keyName: string): any[] {
    return modules.map((mod) => {
        const { name, data = {} } = mod;
        if (!name) {
            throw new Error(`Module is missing name: ${JSON.stringify(mod)}`);
        }
        // If name ends with keyName (case-insensitive), remove that suffix
        // Example: loggerAction -> logger
        const suffix = keyName.toLowerCase();
        let blockTypeName = name;
        if (name.toLowerCase().endsWith(suffix)) {
            blockTypeName = name.slice(0, name.length - suffix.length);
        }
        return {
            [keyName]: blockTypeName,
            ...data,
        };
    });
}

export function simplifyAutomation(automation: Automation) {
    return {
        alias: automation.alias,
        description: automation.description,
        variables: simplifyModules(automation.variables, "variable"),
        triggers: simplifyModules(automation.triggers, "trigger"),
        conditions: simplifyModules(automation.conditions, "condition"),
        actions: simplifyModules(automation.actions, "action"),
        results: simplifyModules(automation.results, "result"),
    };
}

export function exportJson(automation: Automation) {
    return JSON.stringify(simplifyAutomation(automation), null, 2);
}

export function exportYaml(automation: Automation) {
    const simplified = simplifyAutomation(automation);
  return yaml.dump(simplified, { indent: 2, noRefs: true });
}
