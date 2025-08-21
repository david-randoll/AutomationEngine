"use client";

import React, { useState } from "react";
import AddBlockModal from "@/components/AddBlockModal";
import { useFormContext } from "react-hook-form";
import FieldRenderer from "./FieldRenderer";

interface ModuleEditorProps {
    module: ModuleType;
    path: (string | number)[];
}

const ModuleEditor = ({ module, path }: ModuleEditorProps) => {
    const { setValue, getValues } = useFormContext();

    const [modalOpen, setModalOpen] = useState(false);
    const [modalType, setModalType] = useState<Area | null>(null);
    const [modalFieldPath, setModalFieldPath] = useState<(string | number)[] | null>(null);
    const [modalTargetIsArray, setModalTargetIsArray] = useState(false);

    function onAddBlock(blockType: Area, pathInData: (string | number)[], targetIsArray: boolean) {
        setModalFieldPath(pathInData);
        setModalTargetIsArray(targetIsArray);
        setModalType(blockType);
        setModalOpen(true);
    }

    function onModalSelect(modFromServer: ModuleType) {
        if (!modalFieldPath || !modalType) return;

        const { description, ...mod } = modFromServer;
        const instance: ModuleType = {
            ...mod,
            id: modFromServer.id || `m_${Date.now()}_${Math.floor(Math.random() * 1000)}`,
        };

        if (modalTargetIsArray) {
            const current: ModuleType[] = getValues(modalFieldPath.join(".")) || [];
            setValue(modalFieldPath.join("."), [...current, instance], {
                shouldValidate: true,
                shouldDirty: true,
                shouldTouch: true,
            });
        } else {
            setValue(modalFieldPath.join("."), instance, {
                shouldValidate: true,
                shouldDirty: true,
                shouldTouch: true,
            });
        }

        setModalOpen(false);
        setModalFieldPath(null);
        setModalType(null);
        setModalTargetIsArray(false);
    }

    const props = module.schema?.properties || {};

    return (
        <div className="space-y-3">
            <div className="grid grid-cols-1 gap-3">
                {Object.entries(props).map(([key, sch]) => (
                    <FieldRenderer
                        key={key}
                        fieldKey={key}
                        schema={sch}
                        rootSchema={module.schema}
                        pathInData={[...path, key]}
                        onAddBlock={onAddBlock}
                    />
                ))}
            </div>

            <AddBlockModal
                open={modalOpen}
                onOpenChange={(open: boolean) => {
                    if (!open) {
                        setModalOpen(false);
                        setModalFieldPath(null);
                        setModalType(null);
                        setModalTargetIsArray(false);
                    }
                }}
                type={modalType || "action"}
                onSelect={onModalSelect}
            />
        </div>
    );
};

export default ModuleEditor;
