import { useEffect, useMemo, useState } from "react";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { ScrollArea } from "@/components/ui/scroll-area";
import { agent } from "@/lib/agent";
import type { Area, ModuleType } from "@/types/types";

interface AddBlockModalProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    type: Area;
    onSelect: (mod: ModuleType) => void;
}

const AddBlockModal = ({ open, onOpenChange, type, onSelect }: AddBlockModalProps) => {
    const [items, setItems] = useState<ModuleType[]>([]);
    const [loading, setLoading] = useState(false);
    const [filter, setFilter] = useState("");

    useEffect(() => {
        if (!open) return;

        setLoading(true);
        agent
            .get<{ types: ModuleType[] }>(`/automation-engine/block/${type}?includeSchema=true`)
            .then((json) => (Array.isArray(json) ? json : json?.types || []))
            .then(setItems)
            .catch((e) => {
                console.error(e);
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
                        <Input
                            className="w-64 mr-4"
                            placeholder={`Search ${type}s...`}
                            value={filter}
                            onChange={(e) => setFilter(e.target.value)}
                        />
                    </DialogTitle>
                </DialogHeader>

                <div className="mt-4">
                    {loading ? (
                        <div className="p-6 text-center">Loading...</div>
                    ) : (
                        <ScrollArea className="h-96">
                            <div className="grid grid-cols-1 gap-3">
                                {filtered.map((it) => (
                                    <button
                                        key={it.name}
                                        type="button"
                                        className="border rounded p-3 hover:shadow cursor-pointer text-left w-full"
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
                                        {it.schema?.properties ? (
                                            <div className="mt-2 text-xs text-gray-500">
                                                {Object.keys(it.schema.properties as Record<string, unknown>).slice(0, 4).join(", ")}
                                            </div>
                                        ) : null}
                                    </button>
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
