"use client";

import React from "react";
import { Button } from "@/components/ui/button";
import { FaTrash } from "react-icons/fa";
import ModuleEditor from "./ModuleEditor";

interface ModuleListItemProps {
    mod: any;
    isEditing: boolean;
    onEdit: () => void;
    onCloseEdit: () => void;
    path: (string | number)[];
    onRemove: () => void;
}

const ModuleListItem = ({ mod, isEditing, onEdit, onCloseEdit, path, onRemove }: ModuleListItemProps) => {
    return (
        <div className="border rounded p-2 mb-2">
            <div className="flex items-start justify-between">
                <div>
                    <div className="font-medium">{mod.alias || mod.name || "Unnamed"}</div>
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
