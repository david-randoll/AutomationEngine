"use client";

import { Input } from "@/components/ui/input";
import { useFormContext, Controller, useFieldArray } from "react-hook-form";
import { FiEdit, FiTrash2 } from "react-icons/fi";
import { capitalize } from "@/lib/utils";

interface ArrayOfPrimitivesProps {
    name: string;
    title: string;
    itemsType: "string" | "number" | "boolean";
}

/**
 * Array of primitives with Edit and Delete buttons styled like ModuleListItem.
 */
const ArrayOfPrimitives = ({ name, title, itemsType }: ArrayOfPrimitivesProps) => {
    const { control } = useFormContext();
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
                    let newItem: string | number | boolean | null;
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

export default ArrayOfPrimitives;
