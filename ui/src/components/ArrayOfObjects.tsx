"use client";

import React from "react";
import { useFormContext, useFieldArray } from "react-hook-form";
import { FiEdit, FiTrash2 } from "react-icons/fi";
import { capitalize } from "@/lib/utils";
import FieldRenderer from "./FieldRenderer";

/**
 * Array of objects without x-block-type styled with Edit and Delete buttons.
 */
const ArrayOfObjects = ({
    name,
    title,
    itemsSchema,
    pathInData,
    rootSchema,
    onAddBlock,
}: {
    name: string;
    title: string;
    itemsSchema: any;
    pathInData: (string | number)[];
    rootSchema: any;
    onAddBlock: (blockType: Area, pathInData: (string | number)[], targetIsArray: boolean) => void;
}) => {
    const { control } = useFormContext();
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

                    {Object.entries(itemsSchema.properties || {}).map(([childKey, childSchema]) => (
                        <FieldRenderer
                            key={`${name}-${index}-${childKey}`}
                            fieldKey={childKey}
                            schema={childSchema}
                            rootSchema={rootSchema}
                            pathInData={[...pathInData, index, childKey]}
                            onAddBlock={onAddBlock}
                        />
                    ))}
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

export default ArrayOfObjects;
