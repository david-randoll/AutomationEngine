"use client";

import React, { useState } from "react";
import { useFieldArray, useFormContext } from "react-hook-form";
import ModuleListItem from "./ModuleListItem";

interface ModuleListProps {
    title: string;
    area: AreaPlural;
    path: Path;
    blockType: Area; // singular block type of items
    modules: ModuleType[]; // passed from form state for rendering keys (unused directly because of useFieldArray)
}

function capitalize(s: string) {
    if (!s) return s;
    return s.charAt(0).toUpperCase() + s.slice(1);
}

const ModuleList = ({ title, area, path, blockType }: ModuleListProps) => {
    const { control } = useFormContext();
    const fieldName = path.join(".");

    const { fields, append, remove } = useFieldArray({
        control,
        name: fieldName,
        keyName: "reactHookFormId", // avoid clash with your own `id`
    });

    const [editingIdx, setEditingIdx] = useState<number | null>(null);

    // Append a new empty item, adjust fields for your schema defaults if needed
    const handleAdd = () => {
        append({
            id: `m_${Date.now()}_${Math.floor(Math.random() * 1000)}`,
            alias: "",
            description: "",
            variable: "",
        });
        setEditingIdx(fields.length); // open edit mode on newly added
    };

    return (
        <div className="space-y-3">
            <div className="flex justify-between items-center">
                <div className="font-semibold">{title}</div>
            </div>

            {fields.length === 0 && <div className="text-sm text-gray-500">No {title.toLowerCase()} yet.</div>}

            {fields.map((field, index) => (
                <ModuleListItem
                    key={field.id || field.reactHookFormId || index}
                    mod={field}
                    isEditing={editingIdx === index}
                    onEdit={() => setEditingIdx(index)}
                    onCloseEdit={() => setEditingIdx(null)}
                    path={[...path, index]}
                    onRemove={() => {
                        remove(index);
                        setEditingIdx(null);
                    }}
                />
            ))}
        </div>
    );
};

export default ModuleList;
