"use client";

import React from "react";
import { Button } from "@/components/ui/button";
import { FaTrash } from "react-icons/fa";
import ModuleEditor from "./ModuleEditor";
import { useFormContext } from "react-hook-form";
import { Collapsible, CollapsibleTrigger, CollapsibleContent } from "@/components/ui/collapsible";
import { ChevronDown, ChevronRight } from "lucide-react";

interface ModuleListItemProps {
    index: number;
    isEditing: boolean;
    onToggle: () => void;
    path: (string | number)[];
    onRemove: () => void;
}

const ModuleListItem = ({ index, isEditing, onToggle, path, onRemove }: ModuleListItemProps) => {
    const { getValues } = useFormContext();
    const mod = getValues(path.join(".")) as ModuleType;

    return (
        <Collapsible open={isEditing} onOpenChange={onToggle}>
            <div className="border rounded-lg p-3 mb-2 shadow-sm bg-white">
                <div className="flex items-center justify-between">
                    <CollapsibleTrigger asChild>
                        <button className="flex items-center gap-2 text-left w-full hover:text-blue-600 transition-colors">
                            {isEditing ? (
                                <ChevronDown className="h-4 w-4 shrink-0" />
                            ) : (
                                <ChevronRight className="h-4 w-4 shrink-0" />
                            )}
                            <span className="font-medium">{mod?.label || mod?.name || "Unnamed"}</span>
                        </button>
                    </CollapsibleTrigger>
                    <Button variant="destructive" size="sm" onClick={onRemove} className="shrink-0">
                        <FaTrash />
                    </Button>
                </div>

                <CollapsibleContent className="mt-3 space-y-3">
                    <ModuleEditor module={mod} path={path} />
                </CollapsibleContent>
            </div>
        </Collapsible>
    );
};

export default ModuleListItem;
