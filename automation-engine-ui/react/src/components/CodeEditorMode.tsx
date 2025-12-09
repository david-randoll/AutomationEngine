import { useState, useEffect, useCallback } from "react";
import { useFormContext } from "react-hook-form";
import MonacoEditor from "@monaco-editor/react";
import yaml from "js-yaml";
import { Button } from "./ui/button";
import type { Path, JsonSchema } from "@/types/types";
import { FaCopy, FaCheck, FaDownload } from "react-icons/fa";

interface CodeEditorModeProps {
    path: Path;
    schema?: JsonSchema;
}

type CodeMode = "json" | "yaml" | "split";

const CodeEditorMode = ({ path, schema }: CodeEditorModeProps) => {
    const { setValue, getValues } = useFormContext();
    const pathKey = path.join(".");

    const [codeMode, setCodeMode] = useState<CodeMode>("split");
    const [jsonText, setJsonText] = useState<string>("");
    const [yamlText, setYamlText] = useState<string>("");
    const [error, setError] = useState<string | null>(null);
    const [copied, setCopied] = useState<"json" | "yaml" | null>(null);

    // Initialize text from form data
    useEffect(() => {
        const val = getValues(pathKey);
        try {
            setJsonText(JSON.stringify(val, null, 2) || "{}");
            setYamlText(yaml.dump(val) || "");
            setError(null);
        } catch (e) {
            console.error("Failed to serialize data:", e);
            setError("Failed to serialize data");
        }
    }, [getValues, pathKey]);

    // Update form when JSON changes
    const onJsonChange = useCallback(
        (value: string | undefined) => {
            if (!value) return;
            setJsonText(value);

            try {
                const parsed = JSON.parse(value);
                setValue(pathKey, parsed, {
                    shouldValidate: true,
                    shouldDirty: true,
                    shouldTouch: true,
                });
                // Sync YAML
                setYamlText(yaml.dump(parsed));
                setError(null);
            } catch (e) {
                setError(`JSON Error: ${(e as Error).message}`);
            }
        },
        [pathKey, setValue]
    );

    // Update form when YAML changes
    const onYamlChange = useCallback(
        (value: string | undefined) => {
            if (!value) return;
            setYamlText(value);

            try {
                const parsed = yaml.load(value);
                setValue(pathKey, parsed, {
                    shouldValidate: true,
                    shouldDirty: true,
                    shouldTouch: true,
                });
                // Sync JSON
                setJsonText(JSON.stringify(parsed, null, 2));
                setError(null);
            } catch (e) {
                setError(`YAML Error: ${(e as Error).message}`);
            }
        },
        [pathKey, setValue]
    );

    const copyToClipboard = async (text: string, type: "json" | "yaml") => {
        await navigator.clipboard.writeText(text);
        setCopied(type);
        setTimeout(() => setCopied(null), 2000);
    };

    const downloadFile = (text: string, type: "json" | "yaml") => {
        const blob = new Blob([text], { type: "text/plain" });
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `automation.${type}`;
        a.click();
        URL.revokeObjectURL(url);
    };

    const editorOptions = {
        minimap: { enabled: false },
        tabSize: 2,
        fontSize: 14,
        formatOnType: true,
        formatOnPaste: true,
        wordWrap: "on" as const,
        automaticLayout: true,
        scrollBeyondLastLine: false,
        lineNumbers: "on" as const,
        folding: true,
        bracketPairColorization: { enabled: true },
    };

    const renderEditor = (type: "json" | "yaml", height: string = "100%") => (
        <div className="flex flex-col h-full">
            <div className="flex items-center justify-between px-3 py-2 bg-gray-100 border-b border-gray-200">
                <span className="text-sm font-medium text-gray-700 uppercase">{type}</span>
                <div className="flex gap-1">
                    <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => copyToClipboard(type === "json" ? jsonText : yamlText, type)}
                        className="h-7 px-2"
                    >
                        {copied === type ? <FaCheck className="w-3 h-3 text-green-500" /> : <FaCopy className="w-3 h-3" />}
                    </Button>
                    <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => downloadFile(type === "json" ? jsonText : yamlText, type)}
                        className="h-7 px-2"
                    >
                        <FaDownload className="w-3 h-3" />
                    </Button>
                </div>
            </div>
            <div className="flex-1">
                <MonacoEditor
                    height={height}
                    language={type}
                    value={type === "json" ? jsonText : yamlText}
                    onChange={type === "json" ? onJsonChange : onYamlChange}
                    beforeMount={(monaco) => {
                        if (schema && type === "json") {
                            monaco.languages.json.jsonDefaults.setDiagnosticsOptions({
                                validate: true,
                                enableSchemaRequest: false,
                                schemas: [
                                    {
                                        uri: `inmemory://schema/${pathKey}.json`,
                                        fileMatch: ["*"],
                                        schema: schema,
                                    },
                                ],
                            });
                        }
                    }}
                    options={editorOptions}
                />
            </div>
        </div>
    );

    return (
        <div className="flex flex-col h-full bg-white rounded-lg border border-gray-200 shadow-sm overflow-hidden">
            {/* Toolbar */}
            <div className="flex items-center justify-between px-4 py-2 bg-gray-50 border-b border-gray-200">
                <div className="flex items-center gap-2">
                    <span className="text-sm font-medium text-gray-600">View:</span>
                    <div className="inline-flex rounded-md border border-gray-200 bg-white p-0.5">
                        {(["split", "json", "yaml"] as CodeMode[]).map((m) => (
                            <button
                                key={m}
                                onClick={() => setCodeMode(m)}
                                className={`
                                    px-3 py-1 text-xs font-medium rounded transition-all
                                    ${codeMode === m ? "bg-gray-900 text-white" : "text-gray-600 hover:bg-gray-100"}
                                `}
                            >
                                {m === "split" ? "Split" : m.toUpperCase()}
                            </button>
                        ))}
                    </div>
                </div>
                {error && (
                    <div className="text-xs text-red-600 bg-red-50 px-2 py-1 rounded">{error}</div>
                )}
            </div>

            {/* Editor Area */}
            <div className="flex-1 min-h-0">
                {codeMode === "split" ? (
                    <div className="grid grid-cols-2 h-full divide-x divide-gray-200">
                        <div className="h-full">{renderEditor("json")}</div>
                        <div className="h-full">{renderEditor("yaml")}</div>
                    </div>
                ) : (
                    <div className="h-full">{renderEditor(codeMode)}</div>
                )}
            </div>
        </div>
    );
};

export default CodeEditorMode;
