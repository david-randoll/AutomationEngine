"use client";

import React from "react";
import { Button } from "@/components/ui/button";
import { FaTrash } from "react-icons/fa";
import ModuleEditor from "./ModuleEditor";
import { useFormContext } from "react-hook-form";

interface ModuleListItemProps {
    mod: ModuleType;
    isEditing: boolean;
    onEdit: () => void;
    onCloseEdit: () => void;
    path: Path;
}

const ModuleListItem = ({ mod, isEditing, onEdit, onCloseEdit, path }: ModuleListItemProps) => {
    const { setValue, getValues } = useFormContext();

    const handleRemove = () => {
        const arrPath = path.slice(0, -1).join(".");
        const idx = path[path.length - 1] as number;
        const arr = getValues(arrPath) || [];
        const newArr = [...arr];
        newArr.splice(idx, 1);
        setValue(arrPath, newArr);
    };

    return (
        <div className="border rounded p-2 mb-2">
            <div className="flex items-start justify-between">
                <div>
                    <div className="font-medium">{mod.label || mod.name || "Unnamed"}</div>
                </div>
                <div className="flex items-center gap-2">
                    <Button variant="ghost" size="sm" onClick={onEdit}>
                        {isEditing ? "Editing" : "Edit"}
                    </Button>
                    <Button variant="destructive" size="sm" onClick={handleRemove}>
                        <FaTrash />
                    </Button>
                </div>
            </div>

            {isEditing && (
                <div className="mt-3">
                    <ModuleEditor module={mod} path={path} />
                    <div className="mt-3 flex gap-2">
                        <Button onClick={onCloseEdit}>Close</Button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ModuleListItem;
