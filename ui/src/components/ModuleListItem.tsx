"use client";

import React from "react";
import { Button } from "@/components/ui/button";
import { FaTrash } from "react-icons/fa";
import ModuleEditor from "@/components/ModuleEditor";

interface ModuleListItemProps {
    mod: ModuleType;
    isEditing: boolean;
    onEdit: () => void;
    onCloseEdit: () => void;
    onRemove: () => void;
    path: Path; // Path to this module in automation data, used for updates
}

const ModuleListItem = ({ mod, isEditing, onEdit, onCloseEdit, onRemove }: ModuleListItemProps) => {
    return (
        <div className="border rounded p-2">
            <div className="flex items-start justify-between">
                <div>
                    <div className="font-medium">{mod.label || mod.name || "Unnamed"}</div>
                    {mod.description && <div className="text-sm text-gray-500">{mod.description}</div>}
                </div>
                <div className="flex items-center gap-2">
                    <Button variant="ghost" size="sm" onClick={onEdit}>
                        {isEditing ? "Editing" : "Edit"}
                    </Button>
                    <Button variant="destructive" size="sm" onClick={onRemove}>
                        <FaTrash />
                    </Button>
                </div>
            </div>
            {isEditing && (
                <div className="mt-3">
                    <ModuleEditor module={mod} path={[]} /> {/* Pass empty path to edit root of this module */}
                    <div className="mt-3 flex gap-2">
                        <Button onClick={onCloseEdit}>Close</Button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ModuleListItem;
