import React from "react";
import { Button } from "@/components/ui/button";
import { FaTrash, FaPlus } from "react-icons/fa";
import Section from "@/components/Section";
import ModuleEditor from "@/components/ModuleEditor";

interface ModuleListProps {
    title: string;
    modules: ModuleType[];
    area: Area;
    onAdd: () => void;
    onEdit: (idx: number) => void;
    onRemove: (idx: number) => void;
    editing: { area: Area | null; idx: number } | null;
    setEditing: React.Dispatch<React.SetStateAction<{ area: Area | null; idx: number } | null>>;
    onUpdateModule: (idx: number, module: ModuleType) => void;
}

const ModuleList = ({
    title,
    modules,
    area,
    onAdd,
    onEdit,
    onRemove,
    editing,
    setEditing,
    onUpdateModule,
}: ModuleListProps) => {
    return (
        <Section
            title={title}
            extra={
                <Button onClick={onAdd}>
                    <FaPlus /> Add {title.slice(0, -1)}
                </Button>
            }>
            <div className="space-y-3">
                {modules.length === 0 && <div className="text-sm text-gray-500">No {title.toLowerCase()} yet.</div>}
                {modules.map((mod, i) => {
                    const isEditing = editing?.area === area && editing.idx === i;
                    return (
                        <div key={mod.id || i} className="border rounded p-2">
                            <div className="flex items-start justify-between">
                                <div>
                                    <div className="font-medium">{mod.label || mod.name}</div>
                                    {mod.description && <div className="text-sm text-gray-500">{mod.description}</div>}
                                </div>
                                <div className="flex items-center gap-2">
                                    <Button variant="ghost" size="sm" onClick={() => onEdit(i)}>
                                        Edit
                                    </Button>
                                    <Button variant="destructive" size="sm" onClick={() => onRemove(i)}>
                                        <FaTrash />
                                    </Button>
                                </div>
                            </div>
                            {isEditing && (
                                <div className="mt-3">
                                    <ModuleEditor module={mod} onChange={(next) => onUpdateModule(i, next)} />
                                    <div className="mt-3 flex gap-2">
                                        <Button onClick={() => setEditing(null)}>Close</Button>
                                    </div>
                                </div>
                            )}
                        </div>
                    );
                })}
            </div>
        </Section>
    );
};

export default ModuleList;
