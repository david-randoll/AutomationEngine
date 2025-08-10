import React from "react";
import { Button } from "@/components/ui/button";
import { FaPlus } from "react-icons/fa";
import Section from "@/components/Section";
import ModuleListItem from "./ModuleListItem";

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
                {modules.map((mod, i) => (
                    <ModuleListItem
                        key={mod.id || i}
                        mod={mod}
                        idx={i}
                        area={area}
                        isEditing={editing?.area === area && editing.idx === i}
                        onEdit={onEdit}
                        onRemove={onRemove}
                        onUpdateModule={onUpdateModule}
                        setEditing={setEditing}
                    />
                ))}
            </div>
        </Section>
    );
};

export default ModuleList;
