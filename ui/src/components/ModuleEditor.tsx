"use client";

import React, { useState, useEffect } from "react";
import AddBlockModal from "@/components/AddBlockModal";
import { useFormContext } from "react-hook-form";
import FieldRenderer from "./FieldRenderer";
import AdditionalPropertyAdder from "./AdditionalPropertyAdder";

import MonacoEditor from "@monaco-editor/react";
import yaml from "js-yaml";
import { Button } from "./ui/button";
import { agent } from "@/lib/agent";
import { areaToName, moduleToJsonSchema, nameToArea } from "@/lib/utils";
import { useAutomationEngine } from "@/providers/AutomationEngineProvider";

interface ModuleEditorProps {
    module: ModuleType;
    path: Path;
}

const ModuleEditor = ({ module, path }: ModuleEditorProps) => {
    const { setValue, getValues } = useFormContext();
    const [modalOpen, setModalOpen] = useState(false);
    const [modalType, setModalType] = useState<Area | null>(null);
    const [modalFieldPath, setModalFieldPath] = useState<Path | null>(null);
    const [modalTargetIsArray, setModalTargetIsArray] = useState(false);

    const [editMode, setEditMode] = useState<EditMode>("ui");

    const [rawText, setRawText] = useState<string>(() => {
        return JSON.stringify(getValues(path.join(".")), null, 2);
    });

    const { getSchema } = useAutomationEngine();
    const [schema, setSchema] = useState<JsonSchema>();

    const [properties, setProperties] = useState<Record<string, any>>({});

    useEffect(() => {
        const prop = moduleToJsonSchema(module, schema?.properties);
        setProperties({
            ...prop,
        });
        console.log("Schema properties: ", schema?.properties);
        console.log("ModuleEditor: updating properties: ", prop);
    }, [schema?.properties]);

    useEffect(() => {
        const moduleName = module.name ?? areaToName(module);
        console.log("ModuleEditor: fetching schema for", moduleName);
        if (!moduleName) {
            setSchema(module.schema);
            return;
        }

        const pathKey = path.join(".");

        getSchema(pathKey, async () => {
            console.log("ModuleEditor: fetching schema for", moduleName);
            const x = await agent.get<ModuleType>(`/automation-engine/block/${moduleName}/schema`);
            return x.schema;
        })
            .then((schema) => setSchema(schema))
            .catch((e) => {
                console.log("No schema with name:", moduleName);
                setEditMode("json");
            });
    }, [path, module]);

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
            console.error("Failed to switch to UI mode:", err);
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

    function onAddBlock(blockType: Area, pathInData: (string | number)[], targetIsArray: boolean) {
        setModalFieldPath(pathInData);
        setModalTargetIsArray(targetIsArray);
        setModalType(blockType);
        setModalOpen(true);
    }

    function onModalSelect(modFromServer: ModuleType) {
        if (!modalFieldPath || !modalType) return;

        const { name } = modFromServer;
        const instance: ModuleType = {
            ...nameToArea(name),
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
            <div className="mb-4 flex space-x-3">
                {editMode !== "ui" && (
                    <Button onClick={switchToUIMode} variant="outline" size="sm">
                        Edit UI
                    </Button>
                )}
                {editMode !== "json" && (
                    <Button onClick={() => setEditMode("json")} variant="outline" size="sm">
                        Edit JSON
                    </Button>
                )}
                {editMode !== "yaml" && (
                    <Button onClick={() => setEditMode("yaml")} variant="outline" size="sm">
                        Edit YAML
                    </Button>
                )}
            </div>

            {(editMode === "json" || editMode === "yaml") && (
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
            )}

            {editMode === "ui" && (
                <div className="space-y-3">
                    <div className="grid grid-cols-1 gap-3">
                        {Object.entries(properties || {}).map(([key, sch]) => (
                            <FieldRenderer
                                key={key}
                                fieldKey={key}
                                schema={sch}
                                rootSchema={schema}
                                pathInData={[...path, key]}
                                onAddBlock={onAddBlock}
                            />
                        ))}
                    </div>
                    {schema?.additionalProperties && (
                        <AdditionalPropertyAdder properties={properties} setProperties={setProperties} />
                    )}
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
            )}
        </div>
    );
};

export default ModuleEditor;
