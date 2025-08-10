import React from "react";
import { Button } from "@/components/ui/button";
import { FaTrash } from "react-icons/fa";
import ModuleEditor from "@/components/ModuleEditor";

interface ModuleListItemProps {
    mod: ModuleType;
    idx: number;
    area: Area;
    isEditing: boolean;
    onEdit: (idx: number) => void;
    onRemove: (idx: number) => void;
    onUpdateModule: (idx: number, module: ModuleType) => void;
    setEditing: React.Dispatch<React.SetStateAction<{ area: Area | null; idx: number } | null>>;
}

const ModuleListItem = ({
    mod,
    idx,
    area,
    isEditing,
    onEdit,
    onRemove,
    onUpdateModule,
    setEditing,
}: ModuleListItemProps) => {
    return (
        <div key={mod.id || idx} className="border rounded p-2">
            <div className="flex items-start justify-between">
                <div>
                    <div className="font-medium">{mod.label || mod.name}</div>
                    {mod.description && <div className="text-sm text-gray-500">{mod.description}</div>}
                </div>
                <div className="flex items-center gap-2">
                    <Button variant="ghost" size="sm" onClick={() => onEdit(idx)}>
                        Edit
                    </Button>
                    <Button variant="destructive" size="sm" onClick={() => onRemove(idx)}>
                        <FaTrash />
                    </Button>
                </div>
            </div>
            {isEditing && (
                <div className="mt-3">
                    <ModuleEditor module={mod} onChange={(next) => onUpdateModule(idx, next)} />
                    <div className="mt-3 flex gap-2">
                        <Button onClick={() => setEditing(null)}>Close</Button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ModuleListItem;
