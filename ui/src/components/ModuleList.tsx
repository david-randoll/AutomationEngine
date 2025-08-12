"use client";

import React from "react";
import { Button } from "@/components/ui/button";
import { FaPlus } from "react-icons/fa";
import Section from "@/components/Section";
import ModuleListItem from "./ModuleListItem";
import { useAutomation } from "@/context/AutomationContext";

interface ModuleListProps {
    title: string;
    modules: ModuleType[];
    area: Area;
    onAdd: () => void;
    onEdit?: (index: number) => void;
    onRemove?: (index: number) => void;
}

function capitalize(s: string) {
    if (!s) return s;
    return s.charAt(0).toUpperCase() + s.slice(1);
}

const ModuleList = ({ title, modules, area, onAdd, onEdit, onRemove }: ModuleListProps) => {
    const { removeModule, setEditingId } = useAutomation();

    return (
        <Section
            title={title}
            extra={
                <Button onClick={onAdd}>
                    <FaPlus /> Add {capitalize(area)}
                </Button>
            }>
            <div className="space-y-3">
                {modules.length === 0 && <div className="text-sm text-gray-500">No {title.toLowerCase()} yet.</div>}
                {modules.map((mod, i) => {
                    const handleEdit = onEdit ? () => onEdit(i) : () => setEditingId(mod.id);
                    const handleRemove = onRemove ? () => onRemove(i) : () => removeModule(area, i);
                    return (
                        <ModuleListItem
                            key={mod.id || i}
                            mod={mod}
                            idx={i}
                            area={area}
                            isEditing={false} // ModuleListItem will decide via context; we pass a prop but we'll rely on context inside item
                            onEdit={handleEdit}
                            onRemove={handleRemove}
                        />
                    );
                })}
            </div>
        </Section>
    );
};

export default ModuleList;
