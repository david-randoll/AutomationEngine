"use client";

import React, { useState } from "react";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import ModuleList from "@/components/ModuleList";
import AddBlockModal from "@/components/AddBlockModal";
import { useFormContext, Controller, useFieldArray } from "react-hook-form";
import { FiEdit, FiTrash2 } from "react-icons/fi";

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

    /**
     * Array of primitives with Edit and Delete buttons styled like ModuleListItem.
     */
    const ArrayOfPrimitives = ({
        name,
        title,
        itemsType,
    }: {
        name: string;
        title: string;
        itemsType: "string" | "number" | "boolean";
    }) => {
        const { fields, append, remove } = useFieldArray({ control, name });

        return (
            <div className="space-y-2">
                <label className="block font-medium">{capitalize(title)}</label>
                {fields.map((field, index) => (
                    <div
                        key={field.id}
                        className="flex items-center justify-between space-x-3 border rounded p-2 hover:bg-gray-50">
                        <div className="flex-grow">
                            <Controller
                                control={control}
                                name={`${name}.${index}`}
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
                        </div>
                        <div className="flex space-x-2">
                            <button
                                type="button"
                                aria-label="Edit item"
                                className="p-1 hover:text-blue-600"
                                onClick={() => {
                                    // Example edit behavior: for primitives this might focus input or open modal if desired
                                    // (Currently does nothing by default)
                                }}>
                                <FiEdit size={16} />
                            </button>
                            <button
                                type="button"
                                aria-label="Remove item"
                                className="p-1 hover:text-red-600"
                                onClick={() => remove(index)}>
                                <FiTrash2 size={16} />
                            </button>
                        </div>
                    </div>
                ))}
                <button
                    type="button"
                    className="px-3 py-1 text-sm border rounded bg-white hover:shadow"
                    onClick={() => {
                        let newItem: any;
                        if (itemsType === "string") newItem = "";
                        else if (itemsType === "number") newItem = 0;
                        else if (itemsType === "boolean") newItem = false;
                        else newItem = null;
                        append(newItem);
                    }}>
                    Add {capitalize(title)}
                </button>
            </div>
        );
    };

    /**
     * Array of objects without x-block-type styled with Edit and Delete buttons.
     */
    const ArrayOfObjects = ({
        name,
        title,
        itemsSchema,
        pathInData,
    }: {
        name: string;
        title: string;
        itemsSchema: any;
        pathInData: (string | number)[];
    }) => {
        const { fields, append, remove } = useFieldArray({ control, name });

        return (
            <div className="space-y-3 border p-3 rounded">
                <label className="block font-medium">{capitalize(title)}</label>
                {fields.map((field, index) => (
                    <div key={field.id} className="flex flex-col border rounded p-3 space-y-3 hover:shadow">
                        <div className="flex justify-end space-x-2">
                            <button
                                type="button"
                                aria-label="Edit item"
                                className="p-1 hover:text-blue-600"
                                onClick={() => {
                                    // Implement edit logic here if you want (like open modal or autofocus)
                                }}>
                                <FiEdit size={16} />
                            </button>
                            <button
                                type="button"
                                aria-label="Remove item"
                                className="p-1 hover:text-red-600"
                                onClick={() => remove(index)}>
                                <FiTrash2 size={16} />
                            </button>
                        </div>

                        {/* Render object properties */}
                        {Object.entries(itemsSchema.properties || {}).map(([childKey, childSchema]) =>
                            renderField(childKey, childSchema, module.schema, [...pathInData, index, childKey])
                        )}
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
                        append(newItem);
                    }}>
                    Add {capitalize(title)}s
                </button>
            </div>
        );
    };

    /**
     * Recursive function to render fields depending on type.
     */
    function renderField(key: string | number, sch: any, rootSchema: any, pathInData: (string | number)[]) {
        const resolvedSch = resolveSchema(sch, rootSchema);
        const type = resolvedSch?.type || "string";
        const name = pathInData.join(".");
        const title = String(key);

        if (type === "array" && resolvedSch.items) {
            const itemsSchema = resolveSchema(resolvedSch.items, rootSchema);
            const itemsType = itemsSchema.type || "string";

            if (itemsSchema["x-block-type"]) {
                const blockType = itemsSchema["x-block-type"] as Area;
                const arr: ModuleType[] = getValues(name) || [];

                return (
                    <div key={name}>
                        <ModuleList
                            title={capitalize(title)}
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
            }

            if (["string", "number", "boolean"].includes(itemsType)) {
                return (
                    <ArrayOfPrimitives
                        key={name}
                        name={name}
                        title={title}
                        itemsType={itemsType as "string" | "number" | "boolean"}
                    />
                );
            }

            if (itemsType === "object") {
                return (
                    <ArrayOfObjects
                        key={name}
                        name={name}
                        title={title}
                        itemsSchema={itemsSchema}
                        pathInData={pathInData}
                    />
                );
            }

            // fallback to JSON textarea
            return (
                <div key={name}>
                    <label>{capitalize(title)}</label>
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

        if (type === "object" && resolvedSch["x-block-type"]) {
            const blockType = resolvedSch["x-block-type"] as Area;
            const val = getValues(name) ?? null;

            return (
                <div key={name} className="border rounded p-3 mb-4">
                    <ModuleList
                        title={capitalize(title)}
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
                    <label className="block font-medium">{capitalize(title)}</label>
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
                    <span>{capitalize(title)}</span>
                </label>
            );
        }

        return (
            <div key={name}>
                <label className="block text-sm font-medium">{capitalize(title)}</label>
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
            setValue(modalFieldPath.join("."), [...current, instance], {
                shouldValidate: true,
                shouldDirty: true,
                shouldTouch: true,
            });
        } else {
            setValue(modalFieldPath.join("."), instance, {
                shouldValidate: true,
                shouldDirty: true,
                shouldTouch: true,
            });
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
