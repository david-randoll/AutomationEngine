import React, { useState } from "react";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import ModuleList from "@/components/ModuleList";
import ModuleListItem from "@/components/ModuleListItem";
import AddBlockModal from "@/components/AddBlockModal";

interface ModuleEditorProps {
    module: ModuleType;
    onChange: (next: ModuleType) => void;
    editing?: { area: Area | null; idx: number } | null;
    setEditing: React.Dispatch<React.SetStateAction<{ area: Area | null; idx: number } | null>>;
    area?: Area;
}

const ModuleEditor = ({ module, onChange, editing, setEditing, area }: ModuleEditorProps) => {
    const props = module.schema?.properties || {};

    // Modal state for this editor instance
    const [modalOpen, setModalOpen] = useState(false);
    const [modalType, setModalType] = useState<Area | null>(null);
    const [modalAddTargetField, setModalAddTargetField] = useState<string | null>(null);

    function setField(key: string, value: any) {
        onChange({ ...module, data: { ...(module.data || {}), [key]: value } });
    }

    function updateArrayField(fieldName: string, idx: number, nextModule: ModuleType) {
        const arr = (module.data?.[fieldName] || []) as ModuleType[];
        const newArr = arr.slice();
        newArr[idx] = nextModule;
        setField(fieldName, newArr);
    }

    function removeFromArrayField(fieldName: string, idx: number) {
        const arr = (module.data?.[fieldName] || []) as ModuleType[];
        setField(
            fieldName,
            arr.filter((_, i) => i !== idx)
        );
    }

    // When user clicks "Add" on a ModuleList inside this editor,
    // we open the modal and remember the field name (key) and block type
    function onAddClick(fieldName: string, blockType: Area) {
        setModalAddTargetField(fieldName);
        setModalType(blockType);
        setModalOpen(true);
    }

    // When user selects a module type from the modal
    function onModalSelect(mod: ModuleType) {
        if (!modalAddTargetField) return;
        const arr = (module.data?.[modalAddTargetField] || []) as ModuleType[];
        setField(modalAddTargetField, [...arr, mod]);
        setModalOpen(false);
        setModalAddTargetField(null);
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
                {Object.entries(props).map(([key, sch]: any) => {
                    // Array with x-block-type in items: render ModuleList with Add button triggering modal
                    if (sch.type === "array" && sch.items?.["x-block-type"]) {
                        const blockType = sch.items["x-block-type"] as Area;
                        const arrayData: ModuleType[] = module.data?.[key] || [];

                        return (
                            <ModuleList
                                key={key}
                                title={key}
                                area={blockType}
                                modules={arrayData}
                                onAdd={() => onAddClick(key, blockType)} // Use modal for add
                                onEdit={(i) => setEditing && setEditing({ area: blockType, idx: i })}
                                onRemove={(i) => removeFromArrayField(key, i)}
                                editing={editing && editing.area === blockType ? editing : null}
                                setEditing={setEditing!}
                                onUpdateModule={(i, mod) => updateArrayField(key, i, mod)}
                            />
                        );
                    }

                    // Single object with x-block-type
                    if (sch.type === "object" && sch["x-block-type"]) {
                        const blockType = sch["x-block-type"] as Area;
                        const objData = module.data?.[key];
                        if (!objData) {
                            return (
                                <div key={key}>
                                    <label className="block text-sm font-medium">{key}</label>
                                    <button
                                        className="btn btn-sm btn-outline"
                                        onClick={() => {
                                            const newMod: ModuleType = {
                                                id: `id_${Math.random().toString(36).slice(2, 9)}`,
                                                name: blockType,
                                                label: blockType.charAt(0).toUpperCase() + blockType.slice(1),
                                                schema: null,
                                                data: {},
                                            };
                                            setField(key, newMod);
                                        }}>
                                        Add {blockType}
                                    </button>
                                </div>
                            );
                        }

                        return (
                            <div key={key} className="border rounded p-3">
                                <div className="font-semibold mb-2">{key}</div>
                                <ModuleEditor
                                    module={{ ...objData, schema: sch }}
                                    onChange={(next) => setField(key, next)}
                                    editing={editing}
                                    setEditing={setEditing}
                                    area={blockType}
                                />
                            </div>
                        );
                    }

                    // Default primitive fields
                    const type = sch?.type || "string";
                    const title = sch?.title || key;
                    const val = module.data?.[key] ?? sch?.default ?? "";

                    if (type === "boolean") {
                        return (
                            <label key={key} className="inline-flex items-center space-x-2">
                                <input
                                    type="checkbox"
                                    checked={Boolean(val)}
                                    onChange={(e) => setField(key, e.target.checked)}
                                />
                                <span>{title}</span>
                            </label>
                        );
                    }

                    if (type === "number" || type === "integer") {
                        return (
                            <div key={key}>
                                <label className="block text-sm font-medium">{title}</label>
                                <Input
                                    type="number"
                                    value={val}
                                    onChange={(e) =>
                                        setField(key, e.target.value === "" ? null : Number(e.target.value))
                                    }
                                />
                            </div>
                        );
                    }

                    if (type === "object" || type === "array") {
                        return (
                            <div key={key}>
                                <label className="block text-sm font-medium">{title}</label>
                                <Textarea
                                    value={JSON.stringify(val, null, 2)}
                                    onChange={(e) => {
                                        try {
                                            setField(key, JSON.parse(e.target.value));
                                        } catch {
                                            setField(key, e.target.value);
                                        }
                                    }}
                                />
                            </div>
                        );
                    }

                    return (
                        <div key={key}>
                            <label className="block text-sm font-medium">{title}</label>
                            <Input value={val} onChange={(e) => setField(key, e.target.value)} />
                        </div>
                    );
                })}
            </div>

            <AddBlockModal
                open={modalOpen}
                type={modalType || "trigger"}
                onOpenChange={setModalOpen}
                onSelect={onModalSelect}
            />
        </div>
    );
};

export default ModuleEditor;
