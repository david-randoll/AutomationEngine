"use client";

import React, { useState, useEffect } from "react";
import AddBlockModal from "@/components/AddBlockModal";
import { useFormContext } from "react-hook-form";
import FieldRenderer from "./FieldRenderer";
import AdditionalPropertyAdder from "./AdditionalPropertyAdder";

import MonacoEditor from "@monaco-editor/react"; // make sure to install this package
import yaml from "js-yaml";
import { Button } from "./ui/button";

interface ModuleEditorProps {
    module: ModuleType;
    path: (string | number)[];
}

type EditMode = "json" | "yaml" | "ui";

const ModuleEditor = ({ module, path }: ModuleEditorProps) => {
    const { setValue, getValues } = useFormContext();
    const [modalOpen, setModalOpen] = useState(false);
    const [modalType, setModalType] = useState<Area | null>(null);
    const [modalFieldPath, setModalFieldPath] = useState<(string | number)[] | null>(null);
    const [modalTargetIsArray, setModalTargetIsArray] = useState(false);

    const [editMode, setEditMode] = useState<EditMode>("ui");

    const [rawText, setRawText] = useState<string>(() => {
        return JSON.stringify(getValues(path.join(".")), null, 2);
    });

    useEffect(() => {
        if (editMode !== "ui") {
            const val = getValues(path.join("."));
            try {
                setRawText(editMode === "json" ? JSON.stringify(val, null, 2) : yaml.dump(val));
            } catch {
                // ignore error during serialize
            }
        }
    }, [editMode, getValues, path]);

    function switchToUIMode() {
        try {
            const parsed = editMode === "json" ? JSON.parse(rawText) : yaml.load(rawText);
            setValue(path.join("."), parsed, {
                shouldValidate: true,
                shouldDirty: true,
                shouldTouch: true,
            });
            setEditMode("ui");
        } catch (err) {
            alert("Invalid JSON/YAML, cannot switch to UI mode.");
        }
    }

    function onEditorChange(value: string | undefined) {
        if (!value) return;
        setRawText(value);

        try {
            const parsed = editMode === "json" ? JSON.parse(value) : yaml.load(value);
            setValue(path.join("."), parsed, {
                shouldValidate: true,
                shouldDirty: true,
                shouldTouch: true,
            });
        } catch {
            // Invalid JSON/YAML, do nothing to avoid updating form with broken data
        }
    }

    function renderModeButtons() {
        if (editMode === "ui") {
            return (
                <div className="mb-4 flex space-x-3">
                    <Button variant="outline" size="sm" onClick={() => setEditMode("json")}>
                        Edit JSON
                    </Button>
                    <Button variant="outline" size="sm" onClick={() => setEditMode("yaml")}>
                        Edit YAML
                    </Button>
                </div>
            );
        }

        if (editMode === "json") {
            return (
                <div className="mb-4 flex space-x-3">
                    <Button onClick={switchToUIMode} variant="outline" size="sm">
                        Edit UI
                    </Button>
                    <Button onClick={() => setEditMode("yaml")} variant="outline" size="sm">
                        Edit YAML
                    </Button>
                </div>
            );
        }

        // editMode === "yaml"
        return (
            <div className="mb-4 flex space-x-3">
                <Button onClick={switchToUIMode} variant="outline" size="sm">
                    Edit UI
                </Button>
                <Button onClick={() => setEditMode("json")} variant="outline" size="sm">
                    Edit JSON
                </Button>
            </div>
        );
    }

    function renderEditor() {
        if (editMode === "json" || editMode === "yaml") {
            return (
                <MonacoEditor
                    key={editMode}
                    height="400px"
                    defaultLanguage={editMode}
                    value={rawText}
                    onChange={onEditorChange}
                    options={{
                        minimap: { enabled: false },
                        tabSize: 2,
                        formatOnType: true,
                        formatOnPaste: true,
                    }}
                />
            );
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
                {module.schema?.additionalProperties && <AdditionalPropertyAdder path={path} />}
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
    }

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

    return (
        <div>
            {renderModeButtons()}
            {renderEditor()}
        </div>
    );
};

export default ModuleEditor;
