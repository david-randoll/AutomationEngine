"use client";

import React from "react";
import { useFormContext } from "react-hook-form";
import { Button } from "@/components/ui/button";
import PreviewPanel from "@/components/PreviewPanel";
import ModuleEditor from "@/components/ModuleEditor";
import { exportJson, exportYaml } from "@/utils/automation";
import { toast } from "sonner";

interface AutomationBuilderPageProps {
    automationSchema: ModuleType;
}

const AutomationBuilderPage = ({ automationSchema }: AutomationBuilderPageProps) => {
    const { getValues } = useFormContext();

    const handleCopyJson = async () => {
        try {
            await navigator.clipboard.writeText(exportJson(getValues()));
            toast.success("JSON copied to clipboard!");
        } catch (error) {
            console.error("Failed to copy JSON:", error);
            toast.error("Failed to copy JSON.");
        }
    };

    const handleCopyYaml = async () => {
        try {
            await navigator.clipboard.writeText(exportYaml(getValues()));
            toast.success("YAML copied to clipboard!");
        } catch (error) {
            console.error("Failed to copy YAML:", error);
            toast.error("Failed to copy YAML.");
        }
    };

    return (
        <div className="p-6 bg-gray-50 min-h-screen">
            <div className="max-w-6xl mx-auto">
                <header className="flex items-start justify-between mb-6">
                    <div>
                        <h1 className="text-2xl font-bold">Automation Builder</h1>
                        <p className="text-sm text-gray-500">Build automations visually — Home Assistant style.</p>
                    </div>
                    <div className="space-x-2">
                        <Button className="cursor-pointer" onClick={handleCopyJson}>
                            Copy JSON
                        </Button>
                        <Button className="cursor-pointer" variant="outline" onClick={handleCopyYaml}>
                            Copy YAML
                        </Button>
                    </div>
                </header>

                <main className="grid grid-cols-3 gap-6">
                    <section className="col-span-2 space-y-4">
                        <ModuleEditor module={automationSchema} path={[]} />
                    </section>

                    <aside className="col-span-1 space-y-4">
                        <PreviewPanel />
                    </aside>
                </main>
            </div>
        </div>
    );
};

export default AutomationBuilderPage;
