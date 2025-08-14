"use client";

import React, { useState } from "react";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import ModuleList from "@/components/ModuleList";
import AddBlockModal from "@/components/AddBlockModal";
import { useFormContext, Controller } from "react-hook-form";

interface ModuleEditorProps {
    module: ModuleType;
    path: (string | number)[];
}

function capitalize(s: string) {
    if (!s) return s;
    return s.charAt(0).toUpperCase() + s.slice(1);
}

const ModuleEditor = ({ module, path }: ModuleEditorProps) => {
    const { control, setValue, getValues } = useFormContext();

    const [modalOpen, setModalOpen] = useState(false);
    const [modalType, setModalType] = useState<Area | null>(null);
    const [modalFieldPath, setModalFieldPath] = useState<(string | number)[] | null>(null);
    const [modalTargetIsArray, setModalTargetIsArray] = useState(false);

    function resolveSchema(schema: any, rootSchema: any) {
        if (schema?.$ref) {
            const refPath = schema.$ref.replace(/^#\//, "").split("/");
            let resolved: any = rootSchema;
            for (const segment of refPath) resolved = resolved?.[segment];
            return resolved;
        }
        return schema;
    }

    function onAddBlock(blockType: Area, pathInData: (string | number)[], targetIsArray: boolean) {
        setModalFieldPath(pathInData);
        setModalTargetIsArray(targetIsArray);
        setModalType(blockType);
        setModalOpen(true);
    }

    function renderField(key: string | number, sch: any, rootSchema: any, pathInData: (string | number)[]) {
        const resolvedSch = resolveSchema(sch, rootSchema);
        const type = resolvedSch?.type || "string";
        const name = pathInData.join(".");

        if (type === "array" && resolvedSch.items) {
            const itemsSchema = resolveSchema(resolvedSch.items, rootSchema);
            const itemsType = itemsSchema.type || "string";

            if (itemsSchema["x-block-type"]) {
                // Array of blocks with x-block-type => existing UI
                const blockType = itemsSchema["x-block-type"] as Area;
                const arr: ModuleType[] = getValues(name) || [];

                return (
                    <div key={name}>
                        <ModuleList
                            title={capitalize(String(key))}
                            area={`${blockType}s` as AreaPlural}
                            path={pathInData}
                            blockType={blockType}
                            modules={arr}
                        />
                        <button
                            className="inline-flex items-center px-3 py-1.5 border rounded text-sm bg-white hover:shadow mt-2"
                            onClick={() => onAddBlock(blockType, pathInData, true)}
                            type="button">
                            Add {capitalize(blockType)}
                        </button>
                    </div>
                );
            } else if (["string", "number", "boolean"].includes(itemsType)) {
                // Array of primitives
                const arr: any[] = getValues(name) || [];

                return (
                    <div key={name} className="space-y-2">
                        <label className="block font-medium">{capitalize(String(key))}</label>
                        {arr.map((item, i) => (
                            <div key={i} className="flex items-center space-x-2">
                                <Controller
                                    control={control}
                                    name={`${name}.${i}`}
                                    render={({ field }) => {
                                        if (itemsType === "boolean") {
                                            return (
                                                <input
                                                    type="checkbox"
                                                    checked={Boolean(field.value)}
                                                    onChange={(e) => field.onChange(e.target.checked)}
                                                />
                                            );
                                        }
                                        return (
                                            <Input
                                                type={itemsType === "number" ? "number" : "text"}
                                                {...field}
                                                value={field.value ?? ""}
                                                onChange={(e) => {
                                                    if (itemsType === "number") {
                                                        const val = e.target.value;
                                                        field.onChange(val === "" ? "" : +val);
                                                    } else {
                                                        field.onChange(e.target.value);
                                                    }
                                                }}
                                            />
                                        );
                                    }}
                                />
                                <button
                                    type="button"
                                    onClick={() => {
                                        const newArr = [...arr];
                                        newArr.splice(i, 1);
                                        setValue(name, newArr);
                                    }}
                                    className="text-red-500">
                                    Remove
                                </button>
                            </div>
                        ))}

                        <button
                            type="button"
                            className="px-3 py-1 text-sm border rounded bg-white hover:shadow"
                            onClick={() => {
                                let newItem;
                                if (itemsType === "string") newItem = "";
                                else if (itemsType === "number") newItem = 0;
                                else if (itemsType === "boolean") newItem = false;
                                else newItem = null;

                                setValue(name, [...arr, newItem]);
                            }}>
                            Add {capitalize(itemsType)}
                        </button>
                    </div>
                );
            } else if (itemsType === "object") {
                // Array of objects without x-block-type
                const arr: any[] = getValues(name) || [];

                return (
                    <div key={name} className="space-y-3 border p-3 rounded">
                        <label className="block font-medium">{capitalize(String(key))}</label>
                        {arr.map((item, i) => (
                            <div key={i} className="border p-2 rounded space-y-2">
                                {Object.entries(itemsSchema.properties || {}).map(([childKey, childSchema]) =>
                                    renderField(childKey, childSchema, rootSchema, [...pathInData, i, childKey])
                                )}
                                <button
                                    type="button"
                                    onClick={() => {
                                        const newArr = [...arr];
                                        newArr.splice(i, 1);
                                        setValue(name, newArr);
                                    }}
                                    className="text-red-500">
                                    Remove
                                </button>
                            </div>
                        ))}
                        <button
                            type="button"
                            className="px-3 py-1 text-sm border rounded bg-white hover:shadow"
                            onClick={() => {
                                const newItem: any = {};
                                Object.entries(itemsSchema.properties || {}).forEach(([k]) => {
                                    newItem[k] = null;
                                });
                                const newArr = [...arr, newItem];
                                console.log("Adding new item:", newItem, "New array:", newArr);
                                setValue(name, newArr);
                            }}>
                            Add {capitalize(String(key))}
                        </button>
                    </div>
                );
            } else {
                // fallback to JSON textarea for unknown/complex types
                return (
                    <div key={name}>
                        <label>{capitalize(String(key))}</label>
                        <Controller
                            control={control}
                            name={name}
                            render={({ field }) => (
                                <Textarea
                                    rows={4}
                                    {...field}
                                    value={field.value ? JSON.stringify(field.value, null, 2) : ""}
                                    onChange={(e) => {
                                        try {
                                            field.onChange(JSON.parse(e.target.value));
                                        } catch {
                                            field.onChange(e.target.value);
                                        }
                                    }}
                                />
                            )}
                        />
                    </div>
                );
            }
        }

        if (type === "object" && resolvedSch["x-block-type"]) {
            const blockType = resolvedSch["x-block-type"] as Area;
            const val = getValues(name) ?? null;

            return (
                <div key={name} className="border rounded p-3 mb-4">
                    <ModuleList
                        title={capitalize(String(key))}
                        area={`${blockType}s` as AreaPlural}
                        path={pathInData}
                        blockType={blockType}
                        modules={val ? [val] : []}
                    />
                    {!val && (
                        <button
                            className="inline-flex items-center px-3 py-1.5 border rounded text-sm bg-white hover:shadow mt-2"
                            onClick={() => onAddBlock(blockType, pathInData, false)}
                            type="button">
                            Add {capitalize(blockType)}
                        </button>
                    )}
                </div>
            );
        }

        if (type === "object" && resolvedSch.properties) {
            return (
                <div key={name} className="border p-3 rounded space-y-2 mb-4">
                    <label className="block font-medium">{capitalize(String(key))}</label>
                    {Object.entries(resolvedSch.properties).map(([childKey, childSchema]) =>
                        renderField(childKey, childSchema, rootSchema, [...pathInData, childKey])
                    )}
                </div>
            );
        }

        if (type === "boolean") {
            return (
                <label key={name} className="inline-flex items-center space-x-2">
                    <Controller
                        control={control}
                        name={name}
                        render={({ field }) => (
                            <input
                                type="checkbox"
                                {...field}
                                checked={Boolean(field.value)}
                                onChange={(e) => field.onChange(e.target.checked)}
                            />
                        )}
                    />
                    <span>{capitalize(String(key))}</span>
                </label>
            );
        }

        // Default string input field
        return (
            <div key={name}>
                <label className="block text-sm font-medium">{capitalize(String(key))}</label>
                <Controller
                    control={control}
                    name={name}
                    render={({ field }) => (
                        <Input
                            {...field}
                            type="text"
                            onChange={(e) => field.onChange(e.target.value)}
                            value={field.value || ""}
                        />
                    )}
                />
            </div>
        );
    }

    function onModalSelect(modFromServer: ModuleType) {
        if (!modalFieldPath || !modalType) return;

        const instance: ModuleType = {
            ...modFromServer,
            id: modFromServer.id || `m_${Date.now()}_${Math.floor(Math.random() * 1000)}`,
            data: modFromServer.data || {},
        };

        if (modalTargetIsArray) {
            const current: ModuleType[] = getValues(modalFieldPath.join(".")) || [];
            setValue(modalFieldPath.join("."), [...current, instance]);
        } else {
            setValue(modalFieldPath.join("."), instance);
        }

        setModalOpen(false);
        setModalFieldPath(null);
        setModalType(null);
        setModalTargetIsArray(false);
    }

    const props = module.schema?.properties || {};

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
                {Object.entries(props).map(([key, sch]) => renderField(key, sch, module.schema, [...path, key]))}
            </div>

            <AddBlockModal
                open={modalOpen}
                onOpenChange={(open: boolean) => {
                    if (!open) {
                        setModalOpen(false);
                        setModalFieldPath(null);
                        setModalType(null);
                        setModalTargetIsArray(false);
                    }
                }}
                type={modalType || "action"}
                onSelect={onModalSelect}
            />
        </div>
    );
};

export default ModuleEditor;
