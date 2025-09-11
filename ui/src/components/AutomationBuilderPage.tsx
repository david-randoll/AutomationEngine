"use client";

import React, { useEffect, useState } from "react";
import PreviewPanel from "@/components/PreviewPanel";
import ModuleEditor from "@/components/ModuleEditor";
import { agent } from "@/lib/agent";

const AutomationBuilderPage = () => {
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
        <div className="p-4 sm:p-6 bg-gray-50 min-h-screen">
            <div className="max-w-7xl mx-auto">
                <header className="flex flex-col sm:flex-row items-start sm:items-center justify-between mb-6 gap-2 sm:gap-0">
                    <div>
                        <h1 className="text-xl sm:text-2xl font-bold">Automation Builder</h1>
                        <p className="text-sm text-gray-500">Build automations visually — Home Assistant style.</p>
                    </div>
                </header>

                <main className="grid grid-cols-1 md:grid-cols-3 gap-4 md:gap-6">
                    <section className="md:col-span-2 space-y-4">
                        <ModuleEditor
                            module={{
                                schema: automationSchema.schema,
                            }}
                            path={rootPath}
                        />
                    </section>

                    <aside className="md:col-span-1 space-y-4 flex flex-col h-[60vh] md:h-[calc(95vh-6rem)]">
                        <PreviewPanel path={rootPath} />
                    </aside>
                </main>
            </div>
        </div>
    );
};

export default AutomationBuilderPage;
