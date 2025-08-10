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

    function toY(obj: any, indent = 0): string {
        const pad = "  ".repeat(indent);
        if (Array.isArray(obj)) {
            if (obj.length === 0) return "[]\n";
            return (
                obj
                    .map((v) => `${pad}- ${typeof v === "object" ? "\n" + toY(v, indent + 1) : String(v) + "\n"}`)
                    .join("") + (indent === 0 ? "" : "")
            );
        }
        if (obj === null) return "null\n";
        if (typeof obj === "object") {
            return Object.entries(obj)
                .map(([k, v]) =>
                    typeof v === "object" ? `${pad}${k}:\n${toY(v, indent + 1)}` : `${pad}${k}: ${String(v)}\n`
                )
                .join("");
        }
        return `${pad}${String(obj)}\n`;
    }

    return toY(simplified).trim();
}
