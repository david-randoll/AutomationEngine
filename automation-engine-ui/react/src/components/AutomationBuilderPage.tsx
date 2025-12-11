import { useEffect, useState } from "react";
import { useSearch, useNavigate, useLocation } from "@tanstack/react-router";
import PreviewPanel from "@/components/PreviewPanel";
import ModuleEditor from "@/components/ModuleEditor";
import ModeSelector from "@/components/ModeSelector";
import CodeEditorMode from "@/components/CodeEditorMode";
import WorkflowCanvasMode from "@/components/WorkflowCanvasMode";
import { automationDefinitionApi } from "@/lib/automation-api";
import type { Path, ModuleType, UIMode } from "@/types/types";

const AutomationBuilderPage = () => {
    const rootPath: Path = ["root"];
    const [automationSchema, setAutomationSchema] = useState<ModuleType | null>(null);

    // Get mode from URL search params, default to "interactive"
    const search = useSearch({ strict: false });
    const location = useLocation();
    const navigate = useNavigate();
    const currentMode: UIMode = (search as { mode?: UIMode }).mode || "interactive";

    const setMode = (mode: UIMode) => {
        navigate({
            to: location.pathname,
            search: { mode } as Record<string, string>,
        });
    };

    useEffect(() => {
        automationDefinitionApi
            .getSchema()
            .then(setAutomationSchema)
            .catch(console.error);
    }, []);

    if (!automationSchema) return <div>Loading schema...</div>;

    return (
        <div className="p-4 sm:p-6 bg-gray-50 min-h-screen">
            <div className={`mx-auto ${currentMode === "workflow" ? "max-w-full" : "max-w-7xl"}`}>
                <header className="flex flex-col sm:flex-row items-start sm:items-center justify-between mb-6 gap-4">
                    <div>
                        <h1 className="text-xl sm:text-2xl font-bold">Automation Builder</h1>
                        <p className="text-sm text-gray-500">Build automations visually — Home Assistant style.</p>
                    </div>
                    <ModeSelector mode={currentMode} onModeChange={setMode} />
                </header>

                {/* Interactive Mode - Original UI with form + preview */}
                {currentMode === "interactive" && (
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
                )}

                {/* Code Mode - Full-page JSON/YAML editor */}
                {currentMode === "code" && (
                    <main className="h-[calc(100vh-12rem)]">
                        <CodeEditorMode path={rootPath} schema={automationSchema.schema} />
                    </main>
                )}

                {/* Workflow Mode - Visual canvas */}
                {currentMode === "workflow" && (
                    <main className="h-[calc(100vh-12rem)]">
                        <WorkflowCanvasMode path={rootPath} />
                    </main>
                )}
            </div>
        </div>
    );
};

export default AutomationBuilderPage;
