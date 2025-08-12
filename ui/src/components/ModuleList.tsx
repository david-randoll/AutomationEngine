"use client";

import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import { FaPlus } from "react-icons/fa";
import Section from "@/components/Section";
import ModuleListItem from "./ModuleListItem";
import { useAutomation } from "@/context/AutomationContext";

interface ModuleListProps {
    title: string;
    modules: ModuleType[];
    area: AreaPlural; // plural form for root access
    path: Path; // path to this list in automation object, e.g. ["actions"]
}

function capitalize(s: string) {
    if (!s) return s;
    return s.charAt(0).toUpperCase() + s.slice(1);
}

const ModuleList = ({ title, modules, area, path }: ModuleListProps) => {
    const { addModule, removeModule } = useAutomation();
    const [editingIdx, setEditingIdx] = useState<number | null>(null);

    const handleAdd = () => {
        const newModule: ModuleType = {
            id: `new_${Date.now()}`,
            name: "",
            data: {},
        };
        addModule(path, newModule);
        setEditingIdx(modules.length); // edit the newly added
    };

    const handleRemove = (idx: number) => {
        removeModule([...path, idx]);
        // Close editing if the item removed was edited
        if (editingIdx === idx) setEditingIdx(null);
        else if (editingIdx && editingIdx > idx) setEditingIdx(editingIdx - 1);
    };

    return (
        <Section
            title={title}
            extra={
                <Button onClick={handleAdd}>
                    <FaPlus /> Add {capitalize(area.slice(0, -1))}
                </Button>
            }>
            <div className="space-y-3">
                {modules.length === 0 && <div className="text-sm text-gray-500">No {title.toLowerCase()} yet.</div>}
                {modules.map((mod, i) => (
                    <ModuleListItem
                        key={mod.id}
                        mod={mod}
                        isEditing={editingIdx === i}
                        onEdit={() => setEditingIdx(i)}
                        onCloseEdit={() => setEditingIdx(null)}
                        onRemove={() => handleRemove(i)}
                        path={[...path, i]}
                    />
                ))}
            </div>
        </Section>
    );
};

export default ModuleList;
