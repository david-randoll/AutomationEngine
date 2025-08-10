import React from "react";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";

interface ModuleEditorProps {
    module: ModuleType;
    onChange: (next: ModuleType) => void;
}

const ModuleEditor = ({ module, onChange }: ModuleEditorProps) => {
    const props = module.schema?.properties || {};

    function setField(key: string, value: any) {
        onChange({ ...module, data: { ...(module.data || {}), [key]: value } });
    }

    return (
        <div className="space-y-3">
            <div className="flex items-center justify-between">
                <div>
                    <div className="text-lg font-semibold">{module.label || module.name}</div>
                    {module.description && <div className="text-sm text-gray-500">{module.description}</div>}
                </div>
                <div className="text-sm text-gray-400">{module.name}</div>
            </div>

            <div className="grid grid-cols-1 gap-3">
                {Object.entries(props).map(([k, sch]: any) => {
                    const type = sch?.type || "string";
                    const title = sch?.title || k;
                    const val = module.data?.[k] ?? sch?.default ?? "";

                    if (type === "boolean") {
                        return (
                            <label key={k} className="inline-flex items-center space-x-2">
                                <input
                                    type="checkbox"
                                    checked={Boolean(val)}
                                    onChange={(e) => setField(k, e.target.checked)}
                                />
                                <span>{title}</span>
                            </label>
                        );
                    }

                    if (type === "number" || type === "integer") {
                        return (
                            <div key={k}>
                                <label className="block text-sm font-medium">{title}</label>
                                <Input
                                    type="number"
                                    value={val}
                                    onChange={(e) => setField(k, e.target.value === "" ? null : Number(e.target.value))}
                                />
                            </div>
                        );
                    }

                    // default to string / textarea for objects
                    if (type === "object" || type === "array") {
                        return (
                            <div key={k}>
                                <label className="block text-sm font-medium">{title}</label>
                                <Textarea
                                    value={JSON.stringify(val, null, 2)}
                                    onChange={(e) => {
                                        try {
                                            setField(k, JSON.parse(e.target.value));
                                        } catch (err) {
                                            setField(k, e.target.value);
                                        }
                                    }}
                                />
                            </div>
                        );
                    }

                    return (
                        <div key={k}>
                            <label className="block text-sm font-medium">{title}</label>
                            <Input value={val} onChange={(e) => setField(k, e.target.value)} />
                        </div>
                    );
                })}

                {Object.keys(props).length === 0 && (
                    <div className="text-sm text-gray-500">No configurable properties in schema.</div>
                )}
            </div>
        </div>
    );
};

export default ModuleEditor;
