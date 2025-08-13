"use client";

import React from "react";
import { useFormContext } from "react-hook-form";
import { Button } from "@/components/ui/button";
import PreviewPanel from "@/components/PreviewPanel";
import ModuleEditor from "@/components/ModuleEditor";
import { exportJson, exportYaml } from "@/utils/automation";

interface AutomationBuilderPageProps {
    automationSchema: ModuleType; // pass fetched schema as prop
}

const AutomationBuilderPage = ({ automationSchema }: AutomationBuilderPageProps) => {
    const { getValues } = useFormContext();

    // Current live form data
    const automation = getValues() as Automation;

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
                        <ModuleEditor module={automationSchema} path={[]} />
                    </section>

                    <aside className="col-span-1 space-y-4">{/* <PreviewPanel automation={automation} /> */}</aside>
                </main>
            </div>
        </div>
    );
};

export default AutomationBuilderPage;
