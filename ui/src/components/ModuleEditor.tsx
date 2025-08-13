"use client";

import React, { useState } from "react";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import ModuleList from "@/components/ModuleList";
import AddBlockModal from "@/components/AddBlockModal";
import { useFormContext, Controller } from "react-hook-form";

interface ModuleEditorProps {
    module: ModuleType;
    path: Path;
}

function capitalize(s: string) {
    if (!s) return s;
    return s.charAt(0).toUpperCase() + s.slice(1);
}

const ModuleEditor = ({ module, path }: ModuleEditorProps) => {
    const { control, setValue, getValues } = useFormContext();

    const [modalOpen, setModalOpen] = useState(false);
    const [modalType, setModalType] = useState<Area | null>(null);
    const [modalFieldPath, setModalFieldPath] = useState<Path | null>(null);
    const [modalTargetIsArray, setModalTargetIsArray] = useState(false);

    function resolveSchema(schema: any, rootSchema: any) {
        if (schema?.$ref) {
            const refPath = schema.$ref.replace(/^#\//, "").split("/");
            let resolved: any = rootSchema;
            for (const segment of refPath) resolved = resolved?.[segment];
            return { ...resolved, ...schema, $ref: undefined };
        }
        return schema;
    }

    function renderField(key: string | number, sch: any, rootSchema: any, pathInData: Path) {
        const resolvedSch = resolveSchema(sch, rootSchema);
        const type = resolvedSch?.type || "string";
        const val = getValues(pathInData.join("."));

        if (type === "array" && resolvedSch.items) {
            const itemsSchema = resolveSchema(resolvedSch.items, rootSchema);
            if (itemsSchema["x-block-type"]) {
                const blockType = itemsSchema["x-block-type"] as Area;
                const arr: ModuleType[] = val || [];

                return (
                    <div key={String(key)}>
                        <ModuleList
                            title={capitalize(String(key))}
                            area={`${blockType}s` as AreaPlural}
                            modules={arr}
                            path={pathInData}
                        />
                        <button
                            className="inline-flex items-center px-3 py-1.5 border rounded text-sm bg-white hover:shadow"
                            onClick={() => {
                                setModalFieldPath(pathInData);
                                setModalTargetIsArray(true);
                                setModalType(blockType);
                                setModalOpen(true);
                            }}>
                            Add {capitalize(blockType)}
                        </button>
                    </div>
                );
            }

            return (
                <div key={String(key)}>
                    <label>{capitalize(String(key))}</label>
                    <Controller
                        control={control}
                        name={pathInData.join(".")}
                        render={({ field }) => (
                            <Textarea
                                value={JSON.stringify(field.value || [], null, 2)}
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

        if (type === "object" && resolvedSch["x-block-type"]) {
            const blockType = resolvedSch["x-block-type"] as Area;
            return (
                <div key={String(key)} className="border rounded p-3">
                    <ModuleList
                        title={capitalize(String(key))}
                        area={`${blockType}s` as AreaPlural}
                        modules={val ? [val] : []}
                        path={pathInData}
                    />
                    {!val && (
                        <button
                            className="inline-flex items-center px-3 py-1.5 border rounded text-sm bg-white hover:shadow"
                            onClick={() => {
                                setModalFieldPath(pathInData);
                                setModalTargetIsArray(false);
                                setModalType(blockType);
                                setModalOpen(true);
                            }}>
                            Add {capitalize(blockType)}
                        </button>
                    )}
                </div>
            );
        }

        if (type === "object" && resolvedSch.properties) {
            return (
                <div key={String(key)} className="border p-3 rounded space-y-2">
                    <label className="block font-medium">{capitalize(String(key))}</label>
                    {Object.entries(resolvedSch.properties).map(([childKey, childSchema]) =>
                        renderField(childKey, childSchema, rootSchema, [...pathInData, childKey])
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
                        onChange={(e) => setValue(pathInData.join("."), e.target.checked)}
                    />
                    <span>{capitalize(String(key))}</span>
                </label>
            );
        }

        return (
            <div key={String(key)}>
                <label className="block text-sm font-medium">{capitalize(String(key))}</label>
                <Input value={val ?? ""} onChange={(e) => setValue(pathInData.join("."), e.target.value)} />
            </div>
        );
    }

    function onModalSelect(modFromServer: ModuleType) {
        if (!modalFieldPath) return;

        const instance: ModuleType = {
            ...modFromServer,
            id: modFromServer.id || `m_${Date.now()}_${Math.floor(Math.random() * 1000)}`,
            data: modFromServer.data || {},
        };

        const current = getValues(modalFieldPath.join("."));

        if (modalTargetIsArray) {
            setValue(modalFieldPath.join("."), [...(current || []), instance]);
        } else {
            setValue(modalFieldPath.join("."), instance);
        }

        setModalOpen(false);
        setModalFieldPath(null);
        setModalType(null);
        setModalTargetIsArray(false);
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
                type={modalType || "action"}
                onSelect={onModalSelect}
            />
        </div>
    );
};

export default ModuleEditor;
