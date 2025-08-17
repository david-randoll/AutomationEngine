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
        keyName: "reactHookFormId", // avoids collision with your id
    });

    const [editingIdx, setEditingIdx] = useState<number | null>(null);

    return (
        <div className="space-y-3">
            <div className="flex justify-between items-center">
                <div className="font-semibold">{title}</div>
            </div>
            {fields.length === 0 && <div className="text-sm text-gray-500">No {title.toLowerCase()} yet.</div>}
            {fields.map((field, index) => {
                //convert the Record<"reactHookFormId", string> to ModuleType
                const { reactHookFormId, ...mod } = field;
                return (
                    <ModuleListItem
                        key={reactHookFormId || index}
                        mod={mod as ModuleType}
                        isEditing={editingIdx === index}
                        onEdit={() => setEditingIdx(index)}
                        onCloseEdit={() => setEditingIdx(null)}
                        path={[...path, index]}
                        onRemove={() => {
                            remove(index);
                            setEditingIdx(null);
                        }}
                    />
                );
            })}
        </div>
    );
};

export default ModuleList;
