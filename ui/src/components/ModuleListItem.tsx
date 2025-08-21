"use client";

import React from "react";
import { Button } from "@/components/ui/button";
import { FaTrash } from "react-icons/fa";
import ModuleEditor from "./ModuleEditor";
import { useFormContext, useWatch } from "react-hook-form";
import { AccordionItem, AccordionTrigger, AccordionContent } from "@/components/ui/accordion";

interface ModuleListItemProps {
    path: (string | number)[];
    onRemove: () => void;
}

const ModuleListItem = ({ path, onRemove }: ModuleListItemProps) => {
    const { control } = useFormContext();
    const mod = useWatch({
        control,
        name: path.join("."),
    });

    return (
        <AccordionItem value={`${path}`} className="border rounded-lg bg-white shadow-sm">
            <AccordionTrigger className="flex items-center justify-between px-4 py-4 font-medium text-left hover:text-blue-600 transition-colors">
                <span>{mod?.alias || mod?.label || mod?.name || "Unnamed"}</span>
            </AccordionTrigger>

            <AccordionContent className="px-4 pb-4 mt-2">
                <ModuleEditor module={mod} path={path} />
                <div className="flex justify-end mt-4">
                    <Button
                        variant="outline"
                        size="sm"
                        className="flex items-center gap-1 px-2 py-1 text-red-700"
                        onClick={onRemove}>
                        <FaTrash /> Delete
                    </Button>
                </div>
            </AccordionContent>
        </AccordionItem>
    );
};

export default ModuleListItem;
