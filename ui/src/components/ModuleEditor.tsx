"use client";

import React, { useState } from "react";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import ModuleList from "@/components/ModuleList";
import AddBlockModal from "@/components/AddBlockModal";
import { useAutomation } from "@/context/AutomationContext";

interface ModuleEditorProps {
    module: ModuleType;
}

function capitalize(s: string) {
    if (!s) return s;
    return s.charAt(0).toUpperCase() + s.slice(1);
}

const ModuleEditor = ({ module }: ModuleEditorProps) => {
    const { updateModuleById, addChildModule, removeChildModule, editingId, setEditingId } = useAutomation();

    const [modalOpen, setModalOpen] = useState(false);
    const [modalType, setModalType] = useState<Area | null>(null);
    const [modalField, setModalField] = useState<string | null>(null);

    function setField(key: string, value: any) {
        const updated: ModuleType = {
            ...module,
            data: { ...(module.data || {}), [key]: value },
        };
        updateModuleById(module.id, updated);
    }

    function onAddClick(fieldName: string, blockType: Area) {
        setModalField(fieldName);
        setModalType(blockType);
        setModalOpen(true);
    }

    function onModalSelect(modFromServer: any) {
        if (!modalField) return;
        addChildModule(module.id, modalField, {
            ...modFromServer,
            id: undefined,
            data: {},
        });
        setModalOpen(false);
        setModalField(null);
    }

    // --- Schema helpers ---
    function resolveSchema(schema: any, rootSchema: any): any {
        if (schema?.$ref) {
            const refPath = schema.$ref.replace(/^#\//, "").split("/");
            let resolved: any = rootSchema;
            for (const segment of refPath) {
                resolved = resolved?.[segment];
            }
            return { ...resolved, ...schema, $ref: undefined };
        }
        return schema;
    }

    // --- Recursive renderer ---
    function renderField(key: string, sch: any, val: any, setVal: (v: any) => void, rootSchema: any) {
        const resolvedSch = resolveSchema(sch, rootSchema);
        const type = resolvedSch.type || "string";

        // Array with x-block-type
        if (type === "array" && resolvedSch.items) {
            const itemsSchema = resolveSchema(resolvedSch.items, rootSchema);

            if (itemsSchema["x-block-type"]) {
                const blockType = itemsSchema["x-block-type"] as Area;
                const arr: ModuleType[] = val || [];
                return (
                    <ModuleList
                        key={key}
                        title={capitalize(key)}
                        area={blockType}
                        modules={arr}
                        onAdd={() => onAddClick(key, blockType)}
                        onEdit={(i) => setEditingId(arr[i].id)}
                        onRemove={(i) => removeChildModule(module.id, key, i)}
                    />
                );
            }

            // Generic array of objects
            return (
                <div key={key} className="space-y-2">
                    <label className="block font-medium">{capitalize(key)}</label>
                    {(val || []).map((item: any, idx: number) => (
                        <div key={idx} className="border p-2 rounded space-y-2">
                            {Object.entries(itemsSchema.properties || {}).map(([childKey, childSchema]) =>
                                renderField(
                                    childKey,
                                    childSchema,
                                    item?.[childKey],
                                    (v) => {
                                        const newArr = [...(val || [])];
                                        newArr[idx] = {
                                            ...(newArr[idx] || {}),
                                            [childKey]: v,
                                        };
                                        setVal(newArr);
                                    },
                                    rootSchema
                                )
                            )}
                        </div>
                    ))}
                    <ButtonSmall onClick={() => setVal([...(val || []), {}])}>Add {capitalize(key)}</ButtonSmall>
                </div>
            );
        }

        // Single object with x-block-type
        if (type === "object" && resolvedSch["x-block-type"]) {
            const blockType = resolvedSch["x-block-type"] as Area;
            const objVal = val;
            return (
                <div key={key} className="border rounded p-3">
                    <div className="font-semibold mb-2">{capitalize(key)}</div>
                    {objVal && objVal.id ? (
                        <ModuleList
                            title={capitalize(key)}
                            area={blockType}
                            modules={[objVal]}
                            onAdd={() => onAddClick(key, blockType)}
                            onEdit={() => setEditingId(objVal.id)}
                            onRemove={() => setVal(null)}
                        />
                    ) : (
                        <div>
                            <div className="text-sm text-gray-500 mb-2">No {key} configured</div>
                            <ButtonSmall onClick={() => onAddClick(key, blockType)}>
                                Add {capitalize(blockType)}
                            </ButtonSmall>
                        </div>
                    )}
                </div>
            );
        }

        // Generic nested object
        if (type === "object" && resolvedSch.properties) {
            return (
                <div key={key} className="border p-3 rounded space-y-2">
                    <label className="block font-medium">{capitalize(key)}</label>
                    {Object.entries(resolvedSch.properties).map(([childKey, childSchema]) =>
                        renderField(
                            childKey,
                            childSchema,
                            val?.[childKey],
                            (v) =>
                                setVal({
                                    ...(val || {}),
                                    [childKey]: v,
                                }),
                            rootSchema
                        )
                    )}
                </div>
            );
        }

        // Boolean
        if (type === "boolean") {
            return (
                <label key={key} className="inline-flex items-center space-x-2">
                    <input type="checkbox" checked={Boolean(val)} onChange={(e) => setVal(e.target.checked)} />
                    <span>{capitalize(key)}</span>
                </label>
            );
        }

        // Number
        if (type === "number" || type === "integer") {
            return (
                <div key={key}>
                    <label className="block text-sm font-medium">{capitalize(key)}</label>
                    <Input
                        type="number"
                        value={val ?? ""}
                        onChange={(e) => setVal(e.target.value === "" ? null : Number(e.target.value))}
                    />
                </div>
            );
        }

        // Fallback string
        return (
            <div key={key}>
                <label className="block text-sm font-medium">{capitalize(key)}</label>
                <Input value={val ?? ""} onChange={(e) => setVal(e.target.value)} />
            </div>
        );
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
                {Object.entries(module.schema?.properties || {}).map(([key, sch]) =>
                    renderField(key, sch, module.data?.[key], (v) => setField(key, v), module.schema)
                )}
            </div>

            <AddBlockModal
                open={modalOpen}
                onOpenChange={(v) => {
                    if (!v) {
                        setModalOpen(false);
                        setModalField(null);
                    } else setModalOpen(true);
                }}
                type={modalType || "trigger"}
                onSelect={onModalSelect}
            />
        </div>
    );
};

export default ModuleEditor;

function ButtonSmall({ children, onClick }: { children: React.ReactNode; onClick?: () => void }) {
    return (
        <button
            className="inline-flex items-center px-3 py-1.5 border rounded text-sm bg-white hover:shadow"
            onClick={onClick}>
            {children}
        </button>
    );
}
