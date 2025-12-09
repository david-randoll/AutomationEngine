import { useState, useEffect } from "react";
import AddBlockModal from "@/components/AddBlockModal";
import { useFormContext } from "react-hook-form";
import FieldRenderer from "./FieldRenderer";
import AdditionalPropertyAdder from "./AdditionalPropertyAdder";
import MonacoEditor from "@monaco-editor/react";
import yaml from "js-yaml";
import { Button } from "./ui/button";
import { blockApi } from "@/lib/automation-api";
import { areaToName, nameToArea } from "@/lib/utils";
import { useAutomationEngine } from "@/providers/AutomationEngineProvider";
import ExamplesViewer from "./ExamplesViewer";
import ModuleSkeleton from "./ModuleSkeleton";
import type { ModuleType, Path, Area, EditMode, JsonSchema } from "@/types/types";

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

    const pathKey = path.join(".");

    const [rawText, setRawText] = useState<string>(() => {
        return JSON.stringify(getValues(pathKey), null, 2);
    });

    const { getSchema, setSchema: setAutomationSchema, isLoading } = useAutomationEngine();
    const [schema, setSchema] = useState<JsonSchema>();

    /**
     * Infer a JSON Schema type definition from a JavaScript value.
     * Used to reconstruct schema definitions for custom/additional properties.
     */
    function inferSchemaFromValue(value: unknown): JsonSchema {
        if (value === null || value === undefined) {
            return { type: "string" };
        }
        if (Array.isArray(value)) {
            // Try to infer items type from first element
            const itemSchema = value.length > 0 ? inferSchemaFromValue(value[0]) : { type: "string" };
            return { type: "array", items: itemSchema };
        }
        if (typeof value === "object") {
            return { type: "object" };
        }
        if (typeof value === "boolean") {
            return { type: "boolean" };
        }
        if (typeof value === "number") {
            return Number.isInteger(value) ? { type: "integer" } : { type: "number" };
        }
        return { type: "string" };
    }

    /**
     * Merge custom properties from form data into the fetched schema.
     * This ensures that user-added additional properties persist when the schema is refetched.
     */
    function mergeCustomPropertiesIntoSchema(baseSchema: JsonSchema, formData: unknown): JsonSchema {
        // Only merge if additionalProperties is allowed and we have form data
        if (!baseSchema.additionalProperties || !formData || typeof formData !== "object") {
            return baseSchema;
        }

        const schemaProps = (baseSchema.properties as Record<string, unknown>) || {};
        const formDataObj = formData as Record<string, unknown>;
        const customProps: Record<string, JsonSchema> = {};

        // Find properties in form data that aren't defined in the schema
        for (const key of Object.keys(formDataObj)) {
            if (!(key in schemaProps)) {
                customProps[key] = inferSchemaFromValue(formDataObj[key]);
            }
        }

        // If we found custom properties, merge them into the schema
        if (Object.keys(customProps).length > 0) {
            console.log("Merging custom properties into schema:", Object.keys(customProps));
            return {
                ...baseSchema,
                properties: {
                    ...schemaProps,
                    ...customProps,
                },
            };
        }

        return baseSchema;
    }

    useEffect(() => {
        const moduleName = module.name ?? areaToName(module);

        getSchema(pathKey, async () => {
            if (!moduleName) return module.schema;
            console.log("ModuleEditor: fetching schema for", moduleName);

            const res = await blockApi.getSchemaHttp(moduleName);
            if (res.success) {
                return {
                    ...res.data?.schema,
                    examples: res.data?.examples,
                };
            } else {
                console.error("Failed to fetch schema:", res.error?.message);
                return null;
            }
        }).then((sch: JsonSchema | null | unknown) => {
            if (sch) {
                const baseSchema = sch as JsonSchema;
                // Merge any custom properties from form data back into the schema
                const formData = getValues(pathKey);
                const mergedSchema = mergeCustomPropertiesIntoSchema(baseSchema, formData);
                setSchema(mergedSchema);
                // Also update the automation schema cache with the merged version
                if (mergedSchema !== baseSchema) {
                    setAutomationSchema(pathKey, mergedSchema);
                }
            } else {
                console.log("No schema with name:", moduleName);
                setEditMode("json");
            }
        });
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [pathKey]);// don't add module or getSchema here

    useEffect(() => {
        if (editMode !== "ui") {
            console.log("Edit mode changed to", editMode, "serializing data...");
            const val = getValues(pathKey);
            try {
                setRawText(editMode === "json" ? JSON.stringify(val, null, 2) : yaml.dump(val));
            } catch (e) {
                console.error("Failed to serialize data:", e);
            }
        }
    }, [editMode, getValues, path]);

    function switchToUIMode() {
        try {
            console.log("Switching to UI mode...");
            const parsed = editMode === "json" ? JSON.parse(rawText) : yaml.load(rawText);
            setValue(pathKey, parsed, {
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
            setValue(pathKey, parsed, {
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

        const { name, label } = modFromServer;
        const instance: ModuleType = {
            ...nameToArea(name),
            alias: label,
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

    const setProperties = (props: Record<string, unknown>) => {
        //mutate the schema.properties
        const newSchema = {
            ...schema,
            properties: props,
        };

        setSchema(newSchema);
        setAutomationSchema(pathKey, newSchema);
    };

    // Show spinner if schema is loading
    if (isLoading(pathKey)) {
        return <ModuleSkeleton numOfProps={2} />;
    }

    return (
        <div>
            <div className="mb-4 flex space-x-3">
                {editMode !== "ui" && schema && (
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
                <div className="ml-auto">
                    {schema?.examples && Array.isArray(schema.examples) && schema.examples.length > 0 ? (
                        <ExamplesViewer examples={schema.examples as unknown[]} />
                    ) : null}
                </div>
            </div>

            {(editMode === "json" || editMode === "yaml") && (
                <MonacoEditor
                    key={editMode}
                    height="400px"
                    defaultLanguage={editMode}
                    value={rawText}
                    onChange={onEditorChange}
                    beforeMount={(monaco) => {
                        // Register schema only for JSON mode
                        if (schema && editMode === "json") {
                            monaco.languages.json.jsonDefaults.setDiagnosticsOptions({
                                validate: true,
                                enableSchemaRequest: false, // you are providing local schema
                                schemas: [
                                    {
                                        uri: `inmemory://schema/${pathKey}.json`, // any unique URI
                                        fileMatch: ["*"], // or ["editor.json"] if you set a custom model
                                        schema: schema,
                                    },
                                ],
                            });
                        }
                    }}
                    options={{
                        quickSuggestions: true,
                        minimap: { enabled: false },
                        tabSize: 2,
                        fontSize: 14,
                        formatOnType: true,
                        formatOnPaste: true,
                        wordWrap: "on",
                        automaticLayout: true,
                        scrollBeyondLastLine: false,
                    }}
                />
            )}

            {editMode === "ui" && (
                <div className="space-y-3">
                    <div className="grid grid-cols-1 gap-3">
                        {Object.entries((schema?.properties as Record<string, unknown>) || {}).map(([key, sch]) => (
                            <FieldRenderer
                                key={key}
                                fieldKey={key}
                                schema={sch as JsonSchema}
                                rootSchema={schema as JsonSchema}
                                pathInData={[...path, key]}
                                onAddBlock={onAddBlock}
                            />
                        ))}
                    </div>
                    {schema?.additionalProperties ? (
                        <AdditionalPropertyAdder
                            properties={(schema?.properties as Record<string, unknown>) || {}}
                            setProperties={setProperties}
                        />
                    ) : null}
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
