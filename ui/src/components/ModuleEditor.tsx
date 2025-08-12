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
    const { updateModuleById, createModuleInstance, addChildModule, removeChildModule, editingId, setEditingId } =
        useAutomation();

    const [modalOpen, setModalOpen] = useState(false);
    const [modalType, setModalType] = useState<Area | null>(null);
    const [modalField, setModalField] = useState<string | null>(null);

    const props = module.schema?.properties || {};

    function setField(key: string, value: any) {
        const updated: ModuleType = { ...module, data: { ...(module.data || {}), [key]: value } };
        updateModuleById(module.id, updated);
    }

    function onAddClick(fieldName: string, blockType: Area) {
        setModalField(fieldName);
        setModalType(blockType);
        setModalOpen(true);
    }

    function onModalSelect(modFromServer: any) {
        if (!modalField) return;
        // create instance (context helper) and add as child to the module's field array
        addChildModule(module.id, modalField, { ...modFromServer, id: undefined, data: {} });
        setModalOpen(false);
        setModalField(null);
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
                {Object.entries(props).map(([key, sch]: any) => {
                    // Array with items that declare x-block-type: render nested ModuleList
                    if (sch.type === "array" && sch.items?.["x-block-type"]) {
                        const blockType = sch.items["x-block-type"] as Area;
                        const arr: ModuleType[] = (module.data && module.data[key]) || [];

                        return (
                            <div key={key}>
                                <ModuleList
                                    title={capitalize(key)}
                                    area={blockType}
                                    modules={arr}
                                    onAdd={() => onAddClick(key, blockType)}
                                    onEdit={(i) => setEditingId(arr[i].id)}
                                    onRemove={(i) => removeChildModule(module.id, key, i)}
                                />
                            </div>
                        );
                    }

                    // Single object with x-block-type -> treat as a single nested block
                    if (sch.type === "object" && sch["x-block-type"]) {
                        const blockType = sch["x-block-type"] as Area;
                        const objVal = module.data?.[key];

                        return (
                            <div key={key} className="border rounded p-3">
                                <div className="font-semibold mb-2">{capitalize(key)}</div>
                                {objVal && objVal.id ? (
                                    <div>
                                        {/* render the nested module as a list item so it can edit */}
                                        <ModuleList
                                            title={capitalize(key)}
                                            area={blockType}
                                            modules={[objVal]}
                                            onAdd={() => onAddClick(key, blockType)}
                                            onEdit={() => setEditingId(objVal.id)}
                                            onRemove={() => setField(key, null)}
                                        />
                                    </div>
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

                    // Default primitive handling
                    const type = sch?.type || "string";
                    const title = sch?.title || key;
                    const val = module.data?.[key] ?? sch?.default ?? "";

                    if (type === "boolean") {
                        return (
                            <label key={key} className="inline-flex items-center space-x-2">
                                <input
                                    type="checkbox"
                                    checked={Boolean(val)}
                                    onChange={(e) => setField(key, e.target.checked)}
                                />
                                <span>{title}</span>
                            </label>
                        );
                    }

                    if (type === "number" || type === "integer") {
                        return (
                            <div key={key}>
                                <label className="block text-sm font-medium">{title}</label>
                                <Input
                                    type="number"
                                    value={val}
                                    onChange={(e) =>
                                        setField(key, e.target.value === "" ? null : Number(e.target.value))
                                    }
                                />
                            </div>
                        );
                    }

                    if (type === "object" || type === "array") {
                        return (
                            <div key={key}>
                                <label className="block text-sm font-medium">{title}</label>
                                <Textarea
                                    value={JSON.stringify(val, null, 2)}
                                    onChange={(e) => {
                                        try {
                                            setField(key, JSON.parse(e.target.value));
                                        } catch {
                                            setField(key, e.target.value);
                                        }
                                    }}
                                />
                            </div>
                        );
                    }

                    return (
                        <div key={key}>
                            <label className="block text-sm font-medium">{title}</label>
                            <Input value={val} onChange={(e) => setField(key, e.target.value)} />
                        </div>
                    );
                })}
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

/* small Button used inside ModuleEditor for the single-object add CTA */
function ButtonSmall({ children, onClick }: { children: React.ReactNode; onClick?: () => void }) {
    return (
        <button
            className="inline-flex items-center px-3 py-1.5 border rounded text-sm bg-white hover:shadow"
            onClick={onClick}>
            {children}
        </button>
    );
}
