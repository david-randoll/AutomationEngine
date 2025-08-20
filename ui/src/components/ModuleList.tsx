"use client";

import React, { useState } from "react";
import { useFieldArray, useFormContext } from "react-hook-form";
import ModuleListItem from "./ModuleListItem";

interface ModuleListProps {
    title: string;
    path: (string | number)[];
}

const ModuleList = ({ title, path }: ModuleListProps) => {
    const { control } = useFormContext();
    const fieldName = path.join(".");

    const { fields, remove } = useFieldArray({
        control,
        name: fieldName,
        keyName: "reactHookFormId",
    });

    const [editingIdx, setEditingIdx] = useState<number | null>(null);

    return (
        <div className="space-y-3">
            <div className="flex justify-between items-center">
                <div className="font-semibold text-lg">{title}</div>
            </div>

            {fields.length === 0 && <div className="text-sm text-gray-500">No {title.toLowerCase()} yet.</div>}

            {fields.map((field, index) => (
                <ModuleListItem
                    key={field.reactHookFormId}
                    index={index}
                    path={[...path, index]}
                    isEditing={editingIdx === index}
                    onToggle={() => setEditingIdx(editingIdx === index ? null : index)}
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
