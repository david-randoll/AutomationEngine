"use client";

import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import { FaPlus } from "react-icons/fa";
import ModuleListItem from "./ModuleListItem";

interface ModuleListProps {
    title: string;
    modules: ModuleType[];
    area: AreaPlural;
    path: Path;
}

function capitalize(s: string) {
    if (!s) return s;
    return s.charAt(0).toUpperCase() + s.slice(1);
}

const ModuleList = ({ title, modules, area, path }: ModuleListProps) => {
    const [editingIdx, setEditingIdx] = useState<number | null>(null);

    return (
        <div className="space-y-3">
            <div className="flex justify-between items-center">
                <div className="font-semibold">{title}</div>
                <Button
                    onClick={() => {
                        // Initialize empty module array if undefined
                        modules.push({ id: `new_${Date.now()}`, name: "", data: {} });
                        setEditingIdx(modules.length - 1);
                    }}>
                    <FaPlus /> Add {capitalize(area.slice(0, -1))}
                </Button>
            </div>

            {modules.length === 0 && <div className="text-sm text-gray-500">No {title.toLowerCase()} yet.</div>}

            {modules.map((mod, i) => (
                <ModuleListItem
                    key={mod.id}
                    mod={mod}
                    isEditing={editingIdx === i}
                    onEdit={() => setEditingIdx(i)}
                    onCloseEdit={() => setEditingIdx(null)}
                    path={[...path, i]}
                />
            ))}
        </div>
    );
};

export default ModuleList;
