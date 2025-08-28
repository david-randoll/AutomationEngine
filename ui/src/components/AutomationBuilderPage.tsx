"use client";

import React, { useEffect, useState } from "react";
import { useFormContext } from "react-hook-form";
import PreviewPanel from "@/components/PreviewPanel";
import ModuleEditor from "@/components/ModuleEditor";
import { agent } from "@/lib/agent";

const AutomationBuilderPage = () => {
    const { getValues } = useFormContext();
    const rootPath: Path = ["root"];
    const [automationSchema, setAutomationSchema] = useState<ModuleType | null>(null);

    useEffect(() => {
        agent
            .get<ModuleType>("/automation-engine/automation-definition/schema")
            .then(setAutomationSchema)
            .catch(console.error);
    }, []);

    if (!automationSchema) return <div>Loading schema...</div>;

    return (
        <div className="p-6 bg-gray-50 min-h-screen">
            <div className="max-w-6xl mx-auto">
                <header className="flex items-start justify-between mb-6">
                    <div>
                        <h1 className="text-2xl font-bold">Automation Builder</h1>
                        <p className="text-sm text-gray-500">Build automations visually — Home Assistant style.</p>
                    </div>
                </header>

                <main className="grid grid-cols-3 gap-6">
                    <section className="col-span-2 space-y-4">
                        <ModuleEditor
                            module={{
                                schema: automationSchema.schema,
                            }}
                            path={rootPath}
                        />
                    </section>

                    <aside className="col-span-1 space-y-4">
                        <PreviewPanel path={rootPath} />
                    </aside>
                </main>
            </div>
        </div>
    );
};

export default AutomationBuilderPage;
