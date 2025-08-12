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
    // try to use helpers from context if they exist
    const { updateModuleById, addChildModule, createModuleInstance, removeChildModule, editingId, setEditingId } =
        useAutomation();

    const [modalOpen, setModalOpen] = useState(false);
    const [modalType, setModalType] = useState<Area | null>(null);
    const [modalFieldPath, setModalFieldPath] = useState<Path | null>(null);
    const [modalTargetIsArray, setModalTargetIsArray] = useState<boolean>(false);

    // --- Path helpers (immutable updates) ---
    function getAtPath(obj: any, path: Path) {
        let cur = obj;
        for (const seg of path) {
            if (cur == null) return undefined;
            cur = cur[seg as any];
        }
        return cur;
    }

    function setAtPath(obj: any, path: Path, value: any) {
        // shallow clone along the path
        const root = Array.isArray(obj) ? [...obj] : { ...(obj || {}) };
        let cur: any = root;
        for (let i = 0; i < path.length; i++) {
            const seg = path[i];
            const last = i === path.length - 1;
            if (last) {
                // set final
                if (Array.isArray(cur)) {
                    (cur as any)[seg as any] = value;
                } else {
                    (cur as any)[seg as any] = value;
                }
            } else {
                const nextSeg = path[i + 1];
                const existing = cur ? cur[seg as any] : undefined;
                let next;
                if (existing == null) {
                    next = typeof nextSeg === "number" ? [] : {};
                } else {
                    next = Array.isArray(existing) ? [...existing] : { ...existing };
                }
                (cur as any)[seg as any] = next;
                cur = next;
            }
        }
        return root;
    }

    function updateModuleFieldByPath(path: Path, value: any) {
        const newData = setAtPath(module.data || {}, path, value);
        updateModuleById(module.id, { ...module, data: newData });
    }

    function pushIntoArrayAtPath(path: Path, item: any) {
        const arr = (getAtPath(module.data || {}, path) as any[]) || [];
        const newArr = [...arr, item];
        updateModuleFieldByPath(path, newArr);
    }

    function removeFromArrayAtPath(path: Path, idx: number) {
        const arr = (getAtPath(module.data || {}, path) as any[]) || [];
        const newArr = arr.slice(0, idx).concat(arr.slice(idx + 1));
        updateModuleFieldByPath(path, newArr);
    }

    // --- Modal flow for adding a nested block ---
    function onAddClick(path: Path, isArray: boolean, blockType: Area) {
        setModalFieldPath(path);
        setModalTargetIsArray(isArray);
        setModalType(blockType);
        setModalOpen(true);
    }

    function onModalSelect(modFromServer: any) {
        if (!modalFieldPath) return;

        // try to use provided factory if available
        const instance = (createModuleInstance && createModuleInstance(modFromServer)) || {
            ...modFromServer,
            id: `m_${Date.now()}_${Math.floor(Math.random() * 1000)}`,
            data: {},
        };

        // If this path is a simple top-level array field and addChildModule exists, prefer that (keeps old behavior)
        if (
            modalTargetIsArray &&
            modalFieldPath.length === 1 &&
            typeof modalFieldPath[0] === "string" &&
            addChildModule
        ) {
            // addChildModule is expected to add and assign ID internally
            addChildModule(module.id, modalFieldPath[0] as string, instance);
        } else {
            // otherwise perform a local update using path-aware helpers
            if (modalTargetIsArray) {
                pushIntoArrayAtPath(modalFieldPath, instance);
            } else {
                // single object slot -> set the object
                updateModuleFieldByPath(modalFieldPath, instance);
            }
        }

        setModalOpen(false);
        setModalFieldPath(null);
        setModalType(null);
        setModalTargetIsArray(false);
    }

    // --- Schema $ref resolver ---
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
    function renderField(key: string | number, sch: any, rootSchema: any, path: Path) {
        const resolvedSch = resolveSchema(sch, rootSchema);
        const type = resolvedSch?.type || "string";
        const val = getAtPath(module.data || {}, path);

        // Array with x-block-type (render as ModuleList)
        if (type === "array" && resolvedSch.items) {
            const itemsSchema = resolveSchema(resolvedSch.items, rootSchema);

            if (itemsSchema["x-block-type"]) {
                const blockType = itemsSchema["x-block-type"] as Area;
                const arr: ModuleType[] = (val as ModuleType[]) || [];

                return (
                    <div key={String(key)}>
                        <ModuleList
                            title={capitalize(String(key))}
                            area={blockType}
                            modules={arr}
                            onAdd={() => onAddClick(path, true, blockType)}
                            onEdit={(i) => setEditingId(arr[i].id)}
                            onRemove={(i) => {
                                // prefer removeChildModule for top-level arrays if available
                                if (path.length === 1 && typeof path[0] === "string" && removeChildModule) {
                                    removeChildModule(module.id, path[0] as string, i);
                                } else {
                                    removeFromArrayAtPath(path, i);
                                }
                            }}
                        />
                    </div>
                );
            }

            // Generic array of objects/primitives -> iterate and render each item (nested)
            return (
                <div key={String(key)} className="space-y-2">
                    <label className="block font-medium">{capitalize(String(key))}</label>
                    {((val as any[]) || []).map((item: any, idx: number) => (
                        <div key={idx} className="border p-2 rounded space-y-2">
                            {Object.entries(itemsSchema.properties || {}).map(([childKey, childSchema]) =>
                                renderField(childKey, childSchema, rootSchema, [...path, idx, childKey])
                            )}
                        </div>
                    ))}
                    <ButtonSmall onClick={() => pushIntoArrayAtPath(path, {})}>
                        Add {capitalize(String(key))}
                    </ButtonSmall>
                </div>
            );
        }

        // Single object with x-block-type -> treat as nested block slot
        if (type === "object" && resolvedSch["x-block-type"]) {
            const blockType = resolvedSch["x-block-type"] as Area;
            const objVal = val;

            return (
                <div key={String(key)} className="border rounded p-3">
                    <div className="font-semibold mb-2">{capitalize(String(key))}</div>
                    {objVal && objVal.id ? (
                        <ModuleList
                            title={capitalize(String(key))}
                            area={blockType}
                            modules={[objVal]}
                            onAdd={() => onAddClick(path, false, blockType)}
                            onEdit={() => setEditingId(objVal.id)}
                            onRemove={() => updateModuleFieldByPath(path, null)}
                        />
                    ) : (
                        <div>
                            <div className="text-sm text-gray-500 mb-2">No {String(key)} configured</div>
                            <ButtonSmall onClick={() => onAddClick(path, false, blockType)}>
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
                <div key={String(key)} className="border p-3 rounded space-y-2">
                    <label className="block font-medium">{capitalize(String(key))}</label>
                    {Object.entries(resolvedSch.properties).map(([childKey, childSchema]) =>
                        renderField(childKey, childSchema, rootSchema, [...path, childKey])
                    )}
                </div>
            );
        }

        // Boolean
        if (type === "boolean") {
            return (
                <label key={String(key)} className="inline-flex items-center space-x-2">
                    <input
                        type="checkbox"
                        checked={Boolean(val)}
                        onChange={(e) => updateModuleFieldByPath(path, e.target.checked)}
                    />
                    <span>{capitalize(String(key))}</span>
                </label>
            );
        }

        // Number
        if (type === "number" || type === "integer") {
            return (
                <div key={String(key)}>
                    <label className="block text-sm font-medium">{capitalize(String(key))}</label>
                    <Input
                        type="number"
                        value={val ?? ""}
                        onChange={(e) =>
                            updateModuleFieldByPath(path, e.target.value === "" ? null : Number(e.target.value))
                        }
                    />
                </div>
            );
        }

        // Fallback primitive (string)
        // If it's an array/object but we couldn't handle it, show a textarea (keeps previous fallback behavior)
        if (type === "object" || type === "array") {
            return (
                <div key={String(key)}>
                    <label className="block text-sm font-medium">{capitalize(String(key))}</label>
                    <Textarea
                        value={JSON.stringify(val ?? "", null, 2)}
                        onChange={(e) => {
                            try {
                                updateModuleFieldByPath(path, JSON.parse(e.target.value));
                            } catch {
                                updateModuleFieldByPath(path, e.target.value);
                            }
                        }}
                    />
                </div>
            );
        }

        return (
            <div key={String(key)}>
                <label className="block text-sm font-medium">{capitalize(String(key))}</label>
                <Input value={val ?? ""} onChange={(e) => updateModuleFieldByPath(path, e.target.value)} />
            </div>
        );
    }

    // initial render: top-level properties map
    const topProps = module.schema?.properties || {};

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
                {Object.entries(topProps).map(([key, sch]) => renderField(key, sch, module.schema, [key]))}
            </div>

            <AddBlockModal
                open={modalOpen}
                onOpenChange={(v) => {
                    if (!v) {
                        setModalOpen(false);
                        setModalFieldPath(null);
                        setModalType(null);
                        setModalTargetIsArray(false);
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
