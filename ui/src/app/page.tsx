"use client";

import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import AddBlockModal from "@/components/AddBlockModal";
import ModuleList from "@/components/ModuleList";
import PreviewPanel from "@/components/PreviewPanel";
import { useAutomation } from "@/context/AutomationContext";
import { exportJson, exportYaml } from "@/utils/automation";

export default function AutomationBuilderPage() {
    const { automation, addModule } = useAutomation();

    const [modalType, setModalType] = useState<null | Area>(null);

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
                        {(["variable", "trigger", "condition", "action", "result"] as Area[]).map((area) => (
                            <ModuleList
                                key={area}
                                title={area.charAt(0).toUpperCase() + area.slice(1) + "s"}
                                area={area}
                                modules={(automation as any)[area + "s"]}
                                onAdd={() => setModalType(area)}
                            />
                        ))}
                    </section>

                    <aside className="col-span-1 space-y-4">
                        <PreviewPanel automation={automation} />
                    </aside>
                </main>

                <AddBlockModal
                    open={modalType !== null}
                    onOpenChange={(v) => {
                        if (!v) setModalType(null);
                    }}
                    type={modalType || "trigger"}
                    onSelect={(mod) => {
                        // create instance and add top-level (context handles id)
                        addModule(modalType!, { ...(mod as ModuleType), id: undefined, data: {} });
                        setModalType(null);
                    }}
                />
            </div>
        </div>
    );
}
