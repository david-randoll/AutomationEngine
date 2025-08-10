import React, { useEffect, useMemo, useState } from "react";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { ScrollArea } from "@/components/ui/scroll-area";

// Modal chooser component (re-usable)
const AddBlockModal: React.FC<{
    open: boolean;
    onOpenChange: (v: boolean) => void;
    type: "trigger" | "condition" | "action" | "variable" | "result";
    onSelect: (module: ModuleType) => void;
}> = ({ open, onOpenChange, type, onSelect }) => {
    const [items, setItems] = useState<ModuleType[]>([]);
    const [loading, setLoading] = useState(false);
    const [filter, setFilter] = useState("");

    // Fetch helper for module types
    async function fetchModuleTypes(type: string): Promise<ModuleType[]> {
        const url = `http://localhost:8085/automation-engine/block/${type}?includeSchema=true`;
        const res = await fetch(url);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const json = await res.json();
        // The endpoint returns either { types: [...] } or an array; adapt
        if (Array.isArray(json)) return json as ModuleType[];
        if (json && json.types) return json.types as ModuleType[];
        return [];
    }

    useEffect(() => {
        if (!open) return;
        setLoading(true);
        fetchModuleTypes(type)
            .then((r) => setItems(r))
            .catch((e) => {
                console.error("fetch module types", e);
                setItems([]);
            })
            .finally(() => setLoading(false));
    }, [open, type]);

    const filtered = useMemo(() => {
        const f = filter.trim().toLowerCase();
        if (!f) return items;
        return items.filter(
            (it) =>
                (it.label || it.name || "").toLowerCase().includes(f) ||
                (it.description || "").toLowerCase().includes(f)
        );
    }, [items, filter]);

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="max-w-3xl">
                <DialogHeader>
                    <DialogTitle className="flex items-center justify-between">
                        <span>Select {type}</span>
                        <div className="flex items-center gap-2">
                            <Input
                                placeholder={`Search ${type}s...`}
                                value={filter}
                                onChange={(e) => setFilter(e.target.value)}
                            />
                        </div>
                    </DialogTitle>
                </DialogHeader>

                <div className="mt-4">
                    {loading ? (
                        <div className="p-6 text-center">Loading...</div>
                    ) : (
                        <ScrollArea className="h-96">
                            <div className="grid grid-cols-1 gap-3">
                                {filtered.map((it) => (
                                    <div
                                        key={it.name}
                                        className="border rounded p-3 hover:shadow cursor-pointer"
                                        onClick={() => {
                                            onSelect(it);
                                            onOpenChange(false);
                                        }}>
                                        <div className="flex items-center justify-between">
                                            <div>
                                                <div className="font-medium">{it.label || it.name}</div>
                                                {it.description && (
                                                    <div className="text-sm text-gray-500">{it.description}</div>
                                                )}
                                            </div>
                                            <div className="text-sm text-gray-400">{it.name}</div>
                                        </div>
                                        {/* show a small sample of schema props if available */}
                                        {it.schema?.properties && (
                                            <div className="mt-2 text-xs text-gray-500">
                                                {Object.keys(it.schema.properties).slice(0, 4).join(", ")}
                                            </div>
                                        )}
                                    </div>
                                ))}
                                {filtered.length === 0 && (
                                    <div className="p-6 text-center text-gray-500">No results</div>
                                )}
                            </div>
                        </ScrollArea>
                    )}
                </div>
            </DialogContent>
        </Dialog>
    );
};

export default AddBlockModal;
