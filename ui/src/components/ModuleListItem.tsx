"use client";

import React from "react";
import { Button } from "@/components/ui/button";
import { FaTrash } from "react-icons/fa";
import ModuleEditor from "@/components/ModuleEditor";
import { useAutomation } from "@/context/AutomationContext";

interface ModuleListItemProps {
    mod: ModuleType;
    idx: number;
    area: Area;
    isEditing?: boolean; // optional - we derive from context
    onEdit: () => void;
    onRemove: () => void;
}

const ModuleListItem = ({ mod, onEdit, onRemove }: ModuleListItemProps) => {
    const { editingId, setEditingId } = useAutomation();
    const isEditing = editingId === mod.id;

    return (
        <div className="border rounded p-2">
            <div className="flex items-start justify-between">
                <div>
                    <div className="font-medium">{mod.label || mod.name}</div>
                    {mod.description && <div className="text-sm text-gray-500">{mod.description}</div>}
                </div>
                <div className="flex items-center gap-2">
                    <Button variant="ghost" size="sm" onClick={onEdit}>
                        Edit
                    </Button>
                    <Button variant="destructive" size="sm" onClick={onRemove}>
                        <FaTrash />
                    </Button>
                </div>
            </div>
            {isEditing && (
                <div className="mt-3">
                    <ModuleEditor module={mod} />
                    <div className="mt-3 flex gap-2">
                        <Button onClick={() => setEditingId(null)}>Close</Button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ModuleListItem;
