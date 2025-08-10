"use client";
import React, { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { ScrollArea } from "@/components/ui/scroll-area";
import { FaTrash, FaPlus } from "react-icons/fa";

// Lightweight types for the fetched schema
type JsonSchema = any;

type ModuleType = {
    name: string;
    label?: string;
    description?: string;
    schema?: JsonSchema;
};

type Automation = {
    alias?: string;
    description?: string;
    variables: { name: string; value: any }[];
    triggers: ModuleType[];
    conditions: ModuleType[];
    actions: ModuleType[];
    results: ModuleType[];
};

// Utility: generate uid
function uid(prefix = "id") {
    return `${prefix}_${Math.random().toString(36).slice(2, 9)}`;
}

// Section wrapper
const Section: React.FC<{ title: string; extra?: React.ReactNode; children: React.ReactNode }> = ({
    title,
    extra,
    children,
}) => (
    <Card className="mb-6">
        <CardHeader className="flex items-center justify-between">
            <CardTitle className="text-lg font-semibold">{title}</CardTitle>
            {extra}
        </CardHeader>
        <CardContent>{children}</CardContent>
    </Card>
);

// Item card used in lists
const ItemCard: React.FC<{
    label: string;
    description?: string;
    onEdit?: () => void;
    onDelete?: () => void;
}> = ({ label, description, onEdit, onDelete }) => (
    <div className="border rounded p-3 flex items-start justify-between">
        <div>
            <div className="font-medium">{label}</div>
            {description && <div className="text-sm text-gray-500">{description}</div>}
        </div>
        <div className="flex items-center gap-2">
            <Button variant="ghost" size="sm" onClick={onEdit}>
                Edit
            </Button>
            <Button variant="destructive" size="sm" onClick={onDelete}>
                <FaTrash />
            </Button>
        </div>
    </div>
);

// Fetch helper for module types
async function fetchModuleTypes(type: string): Promise<ModuleType[]> {
    const url = `http://localhost:8085/automation-engine/module/${type}?includeSchema=true`;
    const res = await fetch(url);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const json = await res.json();
    // The endpoint returns either { types: [...] } or an array; adapt
    if (Array.isArray(json)) return json as ModuleType[];
    if (json && json.types) return json.types as ModuleType[];
    return [];
}

// Modal chooser component (re-usable)
const AddBlockModal: React.FC<{
    open: boolean;
    onOpenChange: (v: boolean) => void;
    type: "trigger" | "condition" | "action" | "variable" | "result";
    onSelect: (module: ModuleType) => void;
}> = ({ open, onOpenChange, type, onSelect }) => {
    const [items, setItems] = useState<ModuleType[]>([]);
    const [loading, setLoading] = useState(false);
    const [filter, setFilter] = useState("");

    useEffect(() => {
        if (!open) return;
        setLoading(true);
        fetchModuleTypes(type)
            .then((r) => setItems(r))
            .catch((e) => {
                console.error("fetch module types", e);
                setItems([]);
            })
            .finally(() => setLoading(false));
    }, [open, type]);

    const filtered = useMemo(() => {
        const f = filter.trim().toLowerCase();
        if (!f) return items;
        return items.filter(
            (it) =>
                (it.label || it.name || "").toLowerCase().includes(f) ||
                (it.description || "").toLowerCase().includes(f)
        );
    }, [items, filter]);

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="max-w-3xl">
                <DialogHeader>
                    <DialogTitle className="flex items-center justify-between">
                        <span>Select {type}</span>
                        <div className="flex items-center gap-2">
                            <Input
                                placeholder={`Search ${type}s...`}
                                value={filter}
                                onChange={(e) => setFilter(e.target.value)}
                            />
                        </div>
                    </DialogTitle>
                </DialogHeader>

                <div className="mt-4">
                    {loading ? (
                        <div className="p-6 text-center">Loading...</div>
                    ) : (
                        <ScrollArea className="h-96">
                            <div className="grid grid-cols-1 gap-3">
                                {filtered.map((it) => (
                                    <div
                                        key={it.name}
                                        className="border rounded p-3 hover:shadow cursor-pointer"
                                        onClick={() => {
                                            onSelect(it);
                                            onOpenChange(false);
                                        }}>
                                        <div className="flex items-center justify-between">
                                            <div>
                                                <div className="font-medium">{it.label || it.name}</div>
                                                {it.description && (
                                                    <div className="text-sm text-gray-500">{it.description}</div>
                                                )}
                                            </div>
                                            <div className="text-sm text-gray-400">{it.name}</div>
                                        </div>
                                        {/* show a small sample of schema props if available */}
                                        {it.schema?.properties && (
                                            <div className="mt-2 text-xs text-gray-500">
                                                {Object.keys(it.schema.properties).slice(0, 4).join(", ")}
                                            </div>
                                        )}
                                    </div>
                                ))}
                                {filtered.length === 0 && (
                                    <div className="p-6 text-center text-gray-500">No results</div>
                                )}
                            </div>
                        </ScrollArea>
                    )}
                </div>
            </DialogContent>
        </Dialog>
    );
};

// Editor panel for a selected module instance. It edits 'data' object in-place.
const ModuleEditor: React.FC<{
    module: ModuleType & { id?: string; data?: Record<string, any> };
    onChange: (next: ModuleType & { id?: string; data?: Record<string, any> }) => void;
}> = ({ module, onChange }) => {
    const props = module.schema?.properties || {};

    function setField(key: string, value: any) {
        onChange({ ...module, data: { ...(module.data || {}), [key]: value } });
    }

    return (
        <div className="space-y-3">
            <div className="flex items-center justify-between">
                <div>
                    <div className="text-lg font-semibold">{module.label || module.name}</div>
                    {module.description && <div className="text-sm text-gray-500">{module.description}</div>}
                </div>
                <div className="text-sm text-gray-400">{module.name}</div>
            </div>

            <div className="grid grid-cols-1 gap-3">
                {Object.entries(props).map(([k, sch]: any) => {
                    const type = sch?.type || "string";
                    const title = sch?.title || k;
                    const val = module.data?.[k] ?? sch?.default ?? "";

                    if (type === "boolean") {
                        return (
                            <label key={k} className="inline-flex items-center space-x-2">
                                <input
                                    type="checkbox"
                                    checked={Boolean(val)}
                                    onChange={(e) => setField(k, e.target.checked)}
                                />
                                <span>{title}</span>
                            </label>
                        );
                    }

                    if (type === "number" || type === "integer") {
                        return (
                            <div key={k}>
                                <label className="block text-sm font-medium">{title}</label>
                                <Input
                                    type="number"
                                    value={val}
                                    onChange={(e) => setField(k, e.target.value === "" ? null : Number(e.target.value))}
                                />
                            </div>
                        );
                    }

                    // default to string / textarea for objects
                    if (type === "object" || type === "array") {
                        return (
                            <div key={k}>
                                <label className="block text-sm font-medium">{title}</label>
                                <Textarea
                                    value={JSON.stringify(val, null, 2)}
                                    onChange={(e) => {
                                        try {
                                            setField(k, JSON.parse(e.target.value));
                                        } catch (err) {
                                            setField(k, e.target.value);
                                        }
                                    }}
                                />
                            </div>
                        );
                    }

                    return (
                        <div key={k}>
                            <label className="block text-sm font-medium">{title}</label>
                            <Input value={val} onChange={(e) => setField(k, e.target.value)} />
                        </div>
                    );
                })}

                {Object.keys(props).length === 0 && (
                    <div className="text-sm text-gray-500">No configurable properties in schema.</div>
                )}
            </div>
        </div>
    );
};

// Main page component
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

    const [modalType, setModalType] = useState<null | ("trigger" | "condition" | "action" | "variable" | "result")>(
        null
    );
    const [editing, setEditing] = useState<{
        area: "trigger" | "condition" | "action" | "result" | null;
        idx: number;
    } | null>(null);

    // add module to automation
    function addModule(area: "trigger" | "condition" | "action" | "result", mod: ModuleType) {
        const instance = { ...mod, id: uid(mod.name), data: {} } as ModuleType & {
            id: string;
            data: Record<string, any>;
        };
        setAutomation((a) => ({ ...a, [area + "s"]: [...(a as any)[area + "s"], instance] }));
    }

    // add variable
    function addVariable(name: string, value: any) {
        setAutomation((a) => ({ ...a, variables: [...a.variables, { name, value }] }));
    }

    function removeAt<T extends keyof Automation>(area: Exclude<T, "alias" | "description">, idx: number) {
        setAutomation((a) => {
            const arr = (a as any)[area] as any[];
            return { ...a, [area]: arr.filter((_, i) => i !== idx) };
        });
    }

    function updateModule(area: "triggers" | "conditions" | "actions" | "results", idx: number, next: any) {
        setAutomation((a) => {
            const copy = { ...a } as any;
            copy[area] = copy[area].slice();
            copy[area][idx] = next;
            return copy;
        });
    }

    function exportJson() {
        return JSON.stringify(automation, null, 2);
    }

    function exportYaml() {
        // simple converter for our automation shape
        function toY(obj: any, indent = 0) {
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
                    .map(([k, v]) => {
                        if (typeof v === "object") {
                            return `${pad}${k}:\n${toY(v, indent + 1)}`;
                        }
                        return `${pad}${k}: ${String(v)}\n`;
                    })
                    .join("");
            }
            return `${pad}${String(obj)}\n`;
        }
        return toY(automation).trim();
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
                        <Button onClick={() => navigator.clipboard?.writeText(exportJson())}>Copy JSON</Button>
                        <Button variant="outline" onClick={() => navigator.clipboard?.writeText(exportYaml())}>
                            Copy YAML
                        </Button>
                    </div>
                </header>

                <main className="grid grid-cols-3 gap-6">
                    <section className="col-span-2">
                        {/* Variables */}
                        <Section
                            title="Variables"
                            extra={
                                <Button onClick={() => setModalType("variable")}>
                                    <FaPlus /> Add
                                </Button>
                            }>
                            <div className="space-y-3">
                                {automation.variables.map((v, i) => (
                                    <div key={i} className="flex items-center justify-between border rounded p-2">
                                        <div>
                                            <div className="font-medium">{v.name}</div>
                                            <div className="text-sm text-gray-500">{String(v.value)}</div>
                                        </div>
                                        <div className="flex items-center gap-2">
                                            <Button
                                                variant="ghost"
                                                size="sm"
                                                onClick={() => {
                                                    const newName = prompt("Variable name", v.name);
                                                    const newVal = prompt("Value", String(v.value));
                                                    if (newName != null) {
                                                        setAutomation((a) => {
                                                            const copy = { ...a };
                                                            copy.variables = copy.variables.slice();
                                                            copy.variables[i] = { name: newName, value: newVal };
                                                            return copy;
                                                        });
                                                    }
                                                }}>
                                                Edit
                                            </Button>
                                            <Button
                                                variant="destructive"
                                                size="sm"
                                                onClick={() =>
                                                    setAutomation((a) => ({
                                                        ...a,
                                                        variables: a.variables.filter((_, idx) => idx !== i),
                                                    }))
                                                }>
                                                <FaTrash />
                                            </Button>
                                        </div>
                                    </div>
                                ))}

                                {automation.variables.length === 0 && (
                                    <div className="text-sm text-gray-500">No variables yet.</div>
                                )}
                            </div>
                        </Section>

                        {/* Triggers */}
                        <Section
                            title="Triggers"
                            extra={
                                <Button onClick={() => setModalType("trigger")}>
                                    <FaPlus /> Add Trigger
                                </Button>
                            }>
                            <div className="space-y-3">
                                {automation.triggers.map((t, i) => (
                                    <div key={(t as any).id || i} className="border rounded p-2">
                                        <div className="flex items-start justify-between">
                                            <div>
                                                <div className="font-medium">{t.label || t.name}</div>
                                                {t.description && (
                                                    <div className="text-sm text-gray-500">{t.description}</div>
                                                )}
                                            </div>
                                            <div className="flex items-center gap-2">
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    onClick={() => setEditing({ area: "trigger", idx: i })}>
                                                    Edit
                                                </Button>
                                                <Button
                                                    variant="destructive"
                                                    size="sm"
                                                    onClick={() =>
                                                        setAutomation((a) => ({
                                                            ...a,
                                                            triggers: a.triggers.filter((_, idx) => idx !== i),
                                                        }))
                                                    }>
                                                    <FaTrash />
                                                </Button>
                                            </div>
                                        </div>
                                        {editing?.area === "trigger" && editing.idx === i && (
                                            <div className="mt-3">
                                                <ModuleEditor
                                                    module={t as any}
                                                    onChange={(next) => updateModule("triggers", i, next)}
                                                />
                                                <div className="mt-3 flex gap-2">
                                                    <Button onClick={() => setEditing(null)}>Close</Button>
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                ))}

                                {automation.triggers.length === 0 && (
                                    <div className="text-sm text-gray-500">No triggers yet.</div>
                                )}
                            </div>
                        </Section>

                        {/* Conditions */}
                        <Section
                            title="Conditions"
                            extra={
                                <Button onClick={() => setModalType("condition")}>
                                    <FaPlus /> Add Condition
                                </Button>
                            }>
                            <div className="space-y-3">
                                {automation.conditions.map((c, i) => (
                                    <div key={(c as any).id || i} className="border rounded p-2">
                                        <div className="flex items-start justify-between">
                                            <div>
                                                <div className="font-medium">{c.label || c.name}</div>
                                                {c.description && (
                                                    <div className="text-sm text-gray-500">{c.description}</div>
                                                )}
                                            </div>
                                            <div className="flex items-center gap-2">
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    onClick={() => setEditing({ area: "condition", idx: i })}>
                                                    Edit
                                                </Button>
                                                <Button
                                                    variant="destructive"
                                                    size="sm"
                                                    onClick={() =>
                                                        setAutomation((a) => ({
                                                            ...a,
                                                            conditions: a.conditions.filter((_, idx) => idx !== i),
                                                        }))
                                                    }>
                                                    <FaTrash />
                                                </Button>
                                            </div>
                                        </div>
                                        {editing?.area === "condition" && editing.idx === i && (
                                            <div className="mt-3">
                                                <ModuleEditor
                                                    module={c as any}
                                                    onChange={(next) => updateModule("conditions", i, next)}
                                                />
                                                <div className="mt-3 flex gap-2">
                                                    <Button onClick={() => setEditing(null)}>Close</Button>
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                ))}

                                {automation.conditions.length === 0 && (
                                    <div className="text-sm text-gray-500">No conditions yet.</div>
                                )}
                            </div>
                        </Section>

                        {/* Actions */}
                        <Section
                            title="Actions"
                            extra={
                                <Button onClick={() => setModalType("action")}>
                                    <FaPlus /> Add Action
                                </Button>
                            }>
                            <div className="space-y-3">
                                {automation.actions.map((a, i) => (
                                    <div key={(a as any).id || i} className="border rounded p-2">
                                        <div className="flex items-start justify-between">
                                            <div>
                                                <div className="font-medium">{a.label || a.name}</div>
                                                {a.description && (
                                                    <div className="text-sm text-gray-500">{a.description}</div>
                                                )}
                                            </div>
                                            <div className="flex items-center gap-2">
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    onClick={() => setEditing({ area: "action", idx: i })}>
                                                    Edit
                                                </Button>
                                                <Button
                                                    variant="destructive"
                                                    size="sm"
                                                    onClick={() =>
                                                        setAutomation((au) => ({
                                                            ...au,
                                                            actions: au.actions.filter((_, idx) => idx !== i),
                                                        }))
                                                    }>
                                                    <FaTrash />
                                                </Button>
                                            </div>
                                        </div>
                                        {editing?.area === "action" && editing.idx === i && (
                                            <div className="mt-3">
                                                <ModuleEditor
                                                    module={a as any}
                                                    onChange={(next) => updateModule("actions", i, next)}
                                                />
                                                <div className="mt-3 flex gap-2">
                                                    <Button onClick={() => setEditing(null)}>Close</Button>
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                ))}

                                {automation.actions.length === 0 && (
                                    <div className="text-sm text-gray-500">No actions yet.</div>
                                )}
                            </div>
                        </Section>

                        {/* Results */}
                        <Section
                            title="Result (optional)"
                            extra={
                                <Button onClick={() => setModalType("result")}>
                                    <FaPlus /> Add Result
                                </Button>
                            }>
                            <div className="space-y-3">
                                {automation.results.map((r, i) => (
                                    <div key={(r as any).id || i} className="border rounded p-2">
                                        <div className="flex items-start justify-between">
                                            <div>
                                                <div className="font-medium">{r.label || r.name}</div>
                                                {r.description && (
                                                    <div className="text-sm text-gray-500">{r.description}</div>
                                                )}
                                            </div>
                                            <div className="flex items-center gap-2">
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    onClick={() => setEditing({ area: "result", idx: i })}>
                                                    Edit
                                                </Button>
                                                <Button
                                                    variant="destructive"
                                                    size="sm"
                                                    onClick={() =>
                                                        setAutomation((au) => ({
                                                            ...au,
                                                            results: au.results.filter((_, idx) => idx !== i),
                                                        }))
                                                    }>
                                                    <FaTrash />
                                                </Button>
                                            </div>
                                        </div>
                                        {editing?.area === "result" && editing.idx === i && (
                                            <div className="mt-3">
                                                <ModuleEditor
                                                    module={r as any}
                                                    onChange={(next) => updateModule("results", i, next)}
                                                />
                                                <div className="mt-3 flex gap-2">
                                                    <Button onClick={() => setEditing(null)}>Close</Button>
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                ))}

                                {automation.results.length === 0 && (
                                    <div className="text-sm text-gray-500">No results yet.</div>
                                )}
                            </div>
                        </Section>
                    </section>

                    <aside className="col-span-1">
                        <Card>
                            <CardHeader>
                                <CardTitle className="text-lg font-semibold">Preview</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="text-xs text-gray-500 mb-2">JSON</div>
                                <pre className="text-xs bg-gray-100 rounded p-2 max-h-40 overflow-auto">
                                    {exportJson()}
                                </pre>
                                <div className="text-xs text-gray-500 my-2">YAML</div>
                                <pre className="text-xs bg-gray-100 rounded p-2 max-h-40 overflow-auto">
                                    {exportYaml()}
                                </pre>
                            </CardContent>
                        </Card>

                        <div className="mt-4 space-y-2">
                            <Button
                                onClick={() => {
                                    const text = exportJson();
                                    navigator.clipboard?.writeText(text);
                                    alert("Copied JSON to clipboard");
                                }}>
                                Copy JSON
                            </Button>
                            <Button
                                variant="outline"
                                onClick={() => {
                                    const text = exportYaml();
                                    navigator.clipboard?.writeText(text);
                                    alert("Copied YAML to clipboard");
                                }}>
                                Copy YAML
                            </Button>
                        </div>
                    </aside>
                </main>

                {/* Modals */}
                <AddBlockModal
                    open={modalType !== null}
                    onOpenChange={(v) => {
                        if (!v) setModalType(null);
                    }}
                    type={(modalType as any) || "trigger"}
                    onSelect={(mod) => {
                        if (modalType === "variable") {
                            const name = prompt("Variable name", mod.name) || mod.name;
                            const value = prompt("Value (string)", "") || "";
                            addVariable(name, value);
                            setModalType(null);
                            return;
                        }
                        if (modalType === "trigger") addModule("trigger", mod);
                        if (modalType === "condition") addModule("condition", mod);
                        if (modalType === "action") addModule("action", mod);
                        if (modalType === "result") addModule("result", mod);
                    }}
                />
            </div>
        </div>
    );
}
