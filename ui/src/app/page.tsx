"use client";
import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import AddBlockModal from "@/components/AddBlockModal";
import ModuleList from "@/components/ModuleList";
import PreviewPanel from "@/components/PreviewPanel";

// Utility: generate uid
function uid(prefix = "id") {
    return `${prefix}_${Math.random().toString(36).slice(2, 9)}`;
}

export default function AutomationBuilderPage() {
    const [automation, setAutomation] = useState<Automation>({
        alias: "",
        description: "",
        variables: [],
        triggers: [],
        conditions: [],
        actions: [],
        results: [],
    });

    const [modalType, setModalType] = useState<null | Area>(null);
    const [editing, setEditing] = useState<{ area: Area | null; idx: number } | null>(null);

    function addModule(area: Area, mod: ModuleType) {
        const instance = { ...mod, id: uid(mod.name), data: {} };
        setAutomation((a) => ({
            ...a,
            [area + "s"]: [...(a as any)[area + "s"], instance],
        }));
    }

    function removeAt(areaPlural: AreaPlural, idx: number) {
        setAutomation((a) => {
            const arr = (a as any)[areaPlural] as any[];
            return { ...a, [areaPlural]: arr.filter((_, i) => i !== idx) };
        });
    }

    function updateModule(areaPlural: AreaPlural, idx: number, next: ModuleType) {
        setAutomation((a) => {
            const copy = { ...a } as any;
            copy[areaPlural] = copy[areaPlural].slice();
            copy[areaPlural][idx] = next;
            return copy;
        });
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
                        <Button onClick={() => navigator.clipboard?.writeText(JSON.stringify(automation, null, 2))}>
                            Copy JSON
                        </Button>
                        <Button
                            variant="outline"
                            onClick={() => {
                                function toY(obj: any, indent = 0): string {
                                    const pad = "  ".repeat(indent);
                                    if (Array.isArray(obj)) {
                                        if (obj.length === 0) return "[]\n";
                                        return (
                                            obj
                                                .map(
                                                    (v) =>
                                                        `${pad}- ${
                                                            typeof v === "object"
                                                                ? "\n" + toY(v, indent + 1)
                                                                : String(v) + "\n"
                                                        }`
                                                )
                                                .join("") + (indent === 0 ? "" : "")
                                        );
                                    }
                                    if (obj === null) return "null\n";
                                    if (typeof obj === "object") {
                                        return Object.entries(obj)
                                            .map(([k, v]) =>
                                                typeof v === "object"
                                                    ? `${pad}${k}:\n${toY(v, indent + 1)}`
                                                    : `${pad}${k}: ${String(v)}\n`
                                            )
                                            .join("");
                                    }
                                    return `${pad}${String(obj)}\n`;
                                }
                                navigator.clipboard?.writeText(toY(automation).trim());
                            }}>
                            Copy YAML
                        </Button>
                    </div>
                </header>

                <main className="grid grid-cols-3 gap-6">
                    <section className="col-span-2">
                        <ModuleList
                            title="Variables"
                            modules={automation.variables}
                            area="variable"
                            onAdd={() => setModalType("variable")}
                            onEdit={(i) => setEditing({ area: "variable", idx: i })}
                            onRemove={(i) => removeAt("variables", i)}
                            editing={editing}
                            setEditing={setEditing}
                            onUpdateModule={(idx, mod) => updateModule("variables", idx, mod)}
                        />
                        <ModuleList
                            title="Triggers"
                            modules={automation.triggers}
                            area="trigger"
                            onAdd={() => setModalType("trigger")}
                            onEdit={(i) => setEditing({ area: "trigger", idx: i })}
                            onRemove={(i) => removeAt("triggers", i)}
                            editing={editing}
                            setEditing={setEditing}
                            onUpdateModule={(idx, mod) => updateModule("triggers", idx, mod)}
                        />
                        <ModuleList
                            title="Conditions"
                            modules={automation.conditions}
                            area="condition"
                            onAdd={() => setModalType("condition")}
                            onEdit={(i) => setEditing({ area: "condition", idx: i })}
                            onRemove={(i) => removeAt("conditions", i)}
                            editing={editing}
                            setEditing={setEditing}
                            onUpdateModule={(idx, mod) => updateModule("conditions", idx, mod)}
                        />
                        <ModuleList
                            title="Actions"
                            modules={automation.actions}
                            area="action"
                            onAdd={() => setModalType("action")}
                            onEdit={(i) => setEditing({ area: "action", idx: i })}
                            onRemove={(i) => removeAt("actions", i)}
                            editing={editing}
                            setEditing={setEditing}
                            onUpdateModule={(idx, mod) => updateModule("actions", idx, mod)}
                        />
                        <ModuleList
                            title="Results"
                            modules={automation.results}
                            area="result"
                            onAdd={() => setModalType("result")}
                            onEdit={(i) => setEditing({ area: "result", idx: i })}
                            onRemove={(i) => removeAt("results", i)}
                            editing={editing}
                            setEditing={setEditing}
                            onUpdateModule={(idx, mod) => updateModule("results", idx, mod)}
                        />
                    </section>

                    <aside className="col-span-1 space-y-4">
                        <PreviewPanel automation={automation} />
                        <div className="space-y-2">
                            <Button
                                onClick={() => {
                                    const text = JSON.stringify(automation, null, 2);
                                    navigator.clipboard?.writeText(text);
                                    alert("Copied JSON to clipboard");
                                }}>
                                Copy JSON
                            </Button>
                            <Button
                                variant="outline"
                                onClick={() => {
                                    function toY(obj: any, indent = 0): string {
                                        const pad = "  ".repeat(indent);
                                        if (Array.isArray(obj)) {
                                            if (obj.length === 0) return "[]\n";
                                            return (
                                                obj
                                                    .map(
                                                        (v) =>
                                                            `${pad}- ${
                                                                typeof v === "object"
                                                                    ? "\n" + toY(v, indent + 1)
                                                                    : String(v) + "\n"
                                                            }`
                                                    )
                                                    .join("") + (indent === 0 ? "" : "")
                                            );
                                        }
                                        if (obj === null) return "null\n";
                                        if (typeof obj === "object") {
                                            return Object.entries(obj)
                                                .map(([k, v]) =>
                                                    typeof v === "object"
                                                        ? `${pad}${k}:\n${toY(v, indent + 1)}`
                                                        : `${pad}${k}: ${String(v)}\n`
                                                )
                                                .join("");
                                        }
                                        return `${pad}${String(obj)}\n`;
                                    }
                                    const text = toY(automation).trim();
                                    navigator.clipboard?.writeText(text);
                                    alert("Copied YAML to clipboard");
                                }}>
                                Copy YAML
                            </Button>
                        </div>
                    </aside>
                </main>

                <AddBlockModal
                    open={modalType !== null}
                    onOpenChange={(v) => {
                        if (!v) setModalType(null);
                    }}
                    type={modalType || "trigger"}
                    onSelect={(mod) => {
                        addModule(modalType!, mod);
                        setModalType(null);
                    }}
                />
            </div>
        </div>
    );
}
