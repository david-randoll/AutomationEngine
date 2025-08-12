"use client";

import React, { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import PreviewPanel from "@/components/PreviewPanel";
import { useAutomation } from "@/context/AutomationContext";
import { exportJson, exportYaml } from "@/utils/automation";
import ModuleEditor from "@/components/ModuleEditor";

export default function AutomationBuilderPage() {
    const { automation } = useAutomation();
    const [automationSchema, setAutomationSchema] = useState<ModuleType | null>(null);

    async function fetchAutomationSchema(): Promise<ModuleType> {
        const res = await fetch("http://localhost:8085/automation-engine/automation-definition/schema");
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const json = await res.json();
        const definition = json as ModuleType;
        return definition;
    }

    useEffect(() => {
        fetchAutomationSchema()
        .then(setAutomationSchema)
        .catch(console.error);
    }, []);

    if (!automationSchema) {
        return <div className="p-6 text-center">Loading schema...</div>;
    }

    return (
        <div className="p-6 bg-gray-50 min-h-screen">
            <div className="max-w-6xl mx-auto">
                <header className="flex items-start justify-between mb-6">
                    <div>
                        <h1 className="text-2xl font-bold">Automation Builder</h1>
                        <p className="text-sm text-gray-500">Build automations visually — Home Assistant style.</p>
                    </div>
                    <div className="space-x-2">
                        <Button onClick={() => navigator.clipboard.writeText(exportJson(automation))}>Copy JSON</Button>
                        <Button variant="outline" onClick={() => navigator.clipboard.writeText(exportYaml(automation))}>
                            Copy YAML
                        </Button>
                    </div>
                </header>

                <main className="grid grid-cols-3 gap-6">
                    <section className="col-span-2 space-y-4">
                        {/* Pass root automation as module, path=[] means root */}
                        <ModuleEditor module={automationSchema} path={[]} />
                    </section>

                    <aside className="col-span-1 space-y-4">
                        <PreviewPanel automation={automation} />
                    </aside>
                </main>
            </div>
        </div>
    );
}
