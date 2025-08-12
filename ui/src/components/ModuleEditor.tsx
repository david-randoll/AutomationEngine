"use client";

import React, { useState } from "react";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import ModuleList from "@/components/ModuleList";
import AddBlockModal from "@/components/AddBlockModal";
import { useAutomation } from "@/context/AutomationContext";

interface ModuleEditorProps {
    module: ModuleType;
    path: Path; // path to the module in automation root, for updates
}

function capitalize(s: string) {
    if (!s) return s;
    return s.charAt(0).toUpperCase() + s.slice(1);
}

const ModuleEditor = ({ module, path }: ModuleEditorProps) => {
    const { updateModule } = useAutomation();

    const [modalOpen, setModalOpen] = useState(false);
    const [modalType, setModalType] = useState<Area | null>(null);
    const [modalFieldPath, setModalFieldPath] = useState<Path | null>(null);
    const [modalTargetIsArray, setModalTargetIsArray] = useState<boolean>(false);

    function getAtPath(obj: any, path: Path) {
        let cur = obj;
        for (const seg of path) {
            if (cur == null) return undefined;
            cur = cur[seg as any];
        }
        return cur;
    }

    function setAtPath(obj: any, path: Path, value: any) {
        if (path.length === 0) return value;
        const [head, ...rest] = path;
        if (typeof head === "number") {
            const arr = Array.isArray(obj) ? [...obj] : [];
            if (rest.length === 0) {
                arr[head] = value;
                return arr;
            }
            arr[head] = setAtPath(arr[head], rest, value);
            return arr;
        } else {
            const copy = { ...(obj || {}) };
            if (rest.length === 0) {
                copy[head] = value;
                return copy;
            }
            copy[head] = setAtPath(copy[head], rest, value);
            return copy;
        }
    }

    function updateField(pathInData: Path, value: any) {
        console.log("Updating field at path:", [...path, ...pathInData], "with value:", value);
        updateModule([...path, ...pathInData], value);
    }

    function pushIntoArrayAtPath(pathInData: Path, item: any) {
        const arr = (getAtPath(module.data || {}, pathInData) as any[]) || [];
        updateField(pathInData, [...arr, item]);
    }

    function removeFromArrayAtPath(pathInData: Path, idx: number) {
        const arr = (getAtPath(module.data || {}, pathInData) as any[]) || [];
        const newArr = arr.slice(0, idx).concat(arr.slice(idx + 1));
        updateField(pathInData, newArr);
    }

    // Modal flow for adding a nested block
    function onAddClick(pathInData: Path, isArray: boolean, blockType: Area) {
        setModalFieldPath(pathInData);
        setModalTargetIsArray(isArray);
        setModalType(blockType);
        setModalOpen(true);
    }

    function onModalSelect(modFromServer: ModuleType) {
        if (!modalFieldPath) return;
        const instance: ModuleType = {
            ...modFromServer,
            id: modFromServer.id || `m_${Date.now()}_${Math.floor(Math.random() * 1000)}`,
            data: modFromServer.data || {},
        };

        if (modalTargetIsArray) {
            pushIntoArrayAtPath(modalFieldPath, instance);
        } else {
            updateField(modalFieldPath, instance);
        }

        setModalOpen(false);
        setModalFieldPath(null);
        setModalType(null);
        setModalTargetIsArray(false);
    }

    // Resolve $ref schemas
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

    function renderField(key: string | number, sch: any, rootSchema: any, pathInData: Path) {
        const resolvedSch = resolveSchema(sch, rootSchema);
        const type = resolvedSch?.type || "string";
        const val = getAtPath(module.data || {}, pathInData);

        if (type === "array" && resolvedSch.items) {
            const itemsSchema = resolveSchema(resolvedSch.items, rootSchema);

            if (itemsSchema["x-block-type"]) {
                const blockType = itemsSchema["x-block-type"] as Area;
                const arr: ModuleType[] = (val as ModuleType[]) || [];

                return (
                    <div key={String(key)}>
                        <ModuleList
                            title={capitalize(String(key))}
                            area={`${blockType}s` as AreaPlural} // plural to match root keys
                            modules={arr}
                            path={[...path, "data", ...pathInData, key]}
                        />
                        <button
                            className="inline-flex items-center px-3 py-1.5 border rounded text-sm bg-white hover:shadow"
                            onClick={() => onAddClick([...pathInData, key], true, blockType)}>
                            Add {capitalize(blockType)}
                        </button>
                    </div>
                );
            }

            return (
                <div key={String(key)} className="space-y-2">
                    <label className="block font-medium">{capitalize(String(key))}</label>
                    {((val as any[]) || []).map((item: any, idx: number) => (
                        <div key={idx} className="border p-2 rounded space-y-2">
                            {Object.entries(itemsSchema.properties || {}).map(([childKey, childSchema]) =>
                                renderField(childKey, childSchema, rootSchema, [...pathInData, key, idx, childKey])
                            )}
                        </div>
                    ))}
                    <button
                        className="inline-flex items-center px-3 py-1.5 border rounded text-sm bg-white hover:shadow"
                        onClick={() => pushIntoArrayAtPath([...pathInData, key], {})}>
                        Add {capitalize(String(key))}
                    </button>
                </div>
            );
        }

        if (type === "object" && resolvedSch["x-block-type"]) {
            const blockType = resolvedSch["x-block-type"] as Area;
            const objVal = val;

            return (
                <div key={String(key)} className="border rounded p-3">
                    <div className="font-semibold mb-2">{capitalize(String(key))}</div>
                    {objVal && objVal.id ? (
                        <ModuleList
                            title={capitalize(String(key))}
                            area={`${blockType}s` as AreaPlural}
                            modules={[objVal]}
                            path={[...path, "data", ...pathInData, key]}
                        />
                    ) : (
                        <div>
                            <div className="text-sm text-gray-500 mb-2">No {String(key)} configured</div>
                            <button
                                className="inline-flex items-center px-3 py-1.5 border rounded text-sm bg-white hover:shadow"
                                onClick={() => onAddClick([...pathInData, key], false, blockType)}>
                                Add {capitalize(blockType)}
                            </button>
                        </div>
                    )}
                </div>
            );
        }

        if (type === "object" && resolvedSch.properties) {
            return (
                <div key={String(key)} className="border p-3 rounded space-y-2">
                    <label className="block font-medium">{capitalize(String(key))}</label>
                    {Object.entries(resolvedSch.properties).map(([childKey, childSchema]) =>
                        renderField(childKey, childSchema, rootSchema, [...pathInData, key, childKey])
                    )}
                </div>
            );
        }

        if (type === "boolean") {
            return (
                <label key={String(key)} className="inline-flex items-center space-x-2">
                    <input
                        type="checkbox"
                        checked={Boolean(val)}
                        onChange={(e) => updateField([...pathInData, key], e.target.checked)}
                    />
                    <span>{capitalize(String(key))}</span>
                </label>
            );
        }

        if (type === "number" || type === "integer") {
            return (
                <div key={String(key)}>
                    <label className="block text-sm font-medium">{capitalize(String(key))}</label>
                    <Input
                        type="number"
                        value={val ?? ""}
                        onChange={(e) =>
                            updateField([...pathInData, key], e.target.value === "" ? null : Number(e.target.value))
                        }
                    />
                </div>
            );
        }

        if (type === "object" || type === "array") {
            return (
                <div key={String(key)}>
                    <label className="block text-sm font-medium">{capitalize(String(key))}</label>
                    <Textarea
                        value={JSON.stringify(val ?? "", null, 2)}
                        onChange={(e) => {
                            try {
                                updateField([...pathInData, key], JSON.parse(e.target.value));
                            } catch {
                                updateField([...pathInData, key], e.target.value);
                            }
                        }}
                    />
                </div>
            );
        }

        return (
            <div key={String(key)}>
                <label className="block text-sm font-medium">{capitalize(String(key))}</label>
                <Input value={val ?? ""} onChange={(e) => updateField([...pathInData, key], e.target.value)} />
            </div>
        );
    }

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
                    }
                }}
                type={modalType || "action"} // default
                onSelect={onModalSelect}
            />
        </div>
    );
};

export default ModuleEditor;
