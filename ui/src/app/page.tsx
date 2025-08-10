"use client";
import React, { useEffect, useState } from "react";

type JsonSchema = any;

type TypeItem = {
    name: string;
    label?: string;
    description?: string;
    schema?: JsonSchema;
};

type TypesResponse = {
    types: TypeItem[];
};

type Block = {
    id: string;
    typeName: string;
    blockType?: string; // action / condition / trigger / etc
    data: Record<string, any>;
};

function uid(prefix = "b") {
    return `${prefix}_${Math.random().toString(36).slice(2, 9)}`;
}

export default function AutomationBuilder() {
    const [types, setTypes] = useState<TypeItem[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedBlockType, setSelectedBlockType] = useState<string | null>(null);

    const [canvas, setCanvas] = useState<Block[]>([]);
    const [editingBlockId, setEditingBlockId] = useState<string | null>(null);

    useEffect(() => {
        setLoading(true);
        fetch("http://localhost:8085/automation-engine/module/action?includeSchema=true")
            .then((r) => {
                if (!r.ok) throw new Error(`HTTP ${r.status}`);
                return r.json();
            })
            .then((data: TypesResponse) => {
                setTypes(data.types || []);
                setLoading(false);
            })
            .catch((e) => {
                setError(String(e));
                setLoading(false);
            });
    }, []);

    // extract a friendly map grouped by x-block-type
    function groupTypes() {
        const map = new Map<string, TypeItem[]>();
        for (const t of types) {
            const bt = inferBlockType(t) || "misc";
            if (!map.has(bt)) map.set(bt, []);
            map.get(bt)!.push(t);
        }
        return map;
    }

    function inferBlockType(t: TypeItem) {
        // try to find x-block-type on root schema properties or defs
        try {
            if (!t.schema) return undefined;
            // check root properties' items x-block-type
            if (t.schema.properties) {
                for (const [k, v] of Object.entries(t.schema.properties)) {
                    // @ts-ignore
                    if (v && typeof v === "object" && v["x-block-type"]) return v["x-block-type"];
                }
            }
            // check $defs for any with x-block-type
            if (t.schema.$defs) {
                for (const [, def] of Object.entries(t.schema.$defs)) {
                    // @ts-ignore
                    if (def && def["x-block-type"]) return def["x-block-type"];
                }
            }
            // fallback: check top-level x-block-type
            // @ts-ignore
            if (t.schema["x-block-type"]) return t.schema["x-block-type"];
        } catch (e) {}
        return undefined;
    }

    function openModal(blockType?: string) {
        setSelectedBlockType(blockType || null);
        setIsModalOpen(true);
    }

    function addBlock(typeName: string) {
        const t = types.find((x) => x.name === typeName)!;
        const blockType = inferBlockType(t) || "misc";
        const initialData: Record<string, any> = {};
        // populate defaults from schema.properties
        if (t.schema && t.schema.properties) {
            for (const [k, prop] of Object.entries(t.schema.properties)) {
                // @ts-ignore
                if (prop && typeof prop === "object" && prop["default"] !== undefined) initialData[k] = prop["default"];
                else initialData[k] = null;
            }
        }
        const b: Block = { id: uid(), typeName, blockType, data: initialData };
        setCanvas((c) => [...c, b]);
        setIsModalOpen(false);
        setEditingBlockId(b.id);
    }

    function updateBlock(id: string, next: Partial<Block>) {
        setCanvas((c) => c.map((b) => (b.id === id ? { ...b, ...next } : b)));
    }

    function removeBlock(id: string) {
        setCanvas((c) => c.filter((b) => b.id !== id));
        if (editingBlockId === id) setEditingBlockId(null);
    }

    function moveBlock(id: string, dir: "up" | "down") {
        setCanvas((c) => {
            const idx = c.findIndex((x) => x.id === id);
            if (idx < 0) return c;
            const newArr = c.slice();
            const swapWith = dir === "up" ? idx - 1 : idx + 1;
            if (swapWith < 0 || swapWith >= newArr.length) return c;
            const tmp = newArr[swapWith];
            newArr[swapWith] = newArr[idx];
            newArr[idx] = tmp;
            return newArr;
        });
    }

    function renderEditorFor(block: Block) {
        const t = types.find((x) => x.name === block.typeName);
        if (!t || !t.schema) return <div className="p-4">No schema available</div>;
        const props = t.schema.properties || {};
        return (
            <div className="space-y-3 p-4">
                <h3 className="text-lg font-medium">Edit: {t.label || t.name}</h3>
                <p className="text-sm text-gray-500">{t.description}</p>
                <div className="grid grid-cols-1 gap-3">
                    {Object.entries(props).map(([key, propSchema]: any) => {
                        const val = block.data[key];
                        const title = propSchema.title || key;
                        const type = propSchema.type || "string";
                        return (
                            <div key={key} className="flex flex-col">
                                <label className="text-sm font-medium">{title}</label>
                                {type === "string" && (
                                    <input
                                        className="border rounded p-2 mt-1"
                                        value={val ?? ""}
                                        onChange={(e) =>
                                            updateBlock(block.id, { data: { ...block.data, [key]: e.target.value } })
                                        }
                                    />
                                )}
                                {type === "number" && (
                                    <input
                                        type="number"
                                        className="border rounded p-2 mt-1"
                                        value={val ?? ""}
                                        onChange={(e) =>
                                            updateBlock(block.id, {
                                                data: { ...block.data, [key]: e.target.valueAsNumber },
                                            })
                                        }
                                    />
                                )}
                                {type === "boolean" && (
                                    <label className="inline-flex items-center space-x-2 mt-1">
                                        <input
                                            type="checkbox"
                                            checked={Boolean(val)}
                                            onChange={(e) =>
                                                updateBlock(block.id, {
                                                    data: { ...block.data, [key]: e.target.checked },
                                                })
                                            }
                                        />
                                        <span className="text-sm">{propSchema.description || ""}</span>
                                    </label>
                                )}
                                {/* fallback raw */}
                                {!["string", "number", "boolean"].includes(type) && (
                                    <textarea
                                        className="border rounded p-2 mt-1"
                                        value={JSON.stringify(val ?? "", null, 2)}
                                        onChange={(e) => {
                                            try {
                                                const parsed = JSON.parse(e.target.value);
                                                updateBlock(block.id, { data: { ...block.data, [key]: parsed } });
                                            } catch (err) {
                                                // ignore parse error — keep raw text
                                                updateBlock(block.id, {
                                                    data: { ...block.data, [key]: e.target.value },
                                                });
                                            }
                                        }}
                                    />
                                )}
                            </div>
                        );
                    })}

                    <div className="flex items-center space-x-2">
                        <button
                            className="px-3 py-1 bg-red-500 text-white rounded"
                            onClick={() => removeBlock(block.id)}>
                            Delete
                        </button>
                        <button className="px-3 py-1 bg-gray-200 rounded" onClick={() => moveBlock(block.id, "up")}>
                            Move Up
                        </button>
                        <button className="px-3 py-1 bg-gray-200 rounded" onClick={() => moveBlock(block.id, "down")}>
                            Move Down
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    function exportJson() {
        // Build a compact automation structure — naive grouping by blockType
        const payload = {
            automations: canvas.map((b) => ({ type: b.typeName, data: b.data })),
        };
        return JSON.stringify(payload, null, 2);
    }

    function exportYaml() {
        // tiny YAML converter for simple objects
        const json = JSON.parse(exportJson());
        function toY(obj: any, indent = 0) {
            const pad = "  ".repeat(indent);
            if (Array.isArray(obj)) {
                return obj.map((v) => `${pad}- ${toY(v, indent + 1).trimStart()}`).join("\n");
            }
            if (obj === null) return "null\n";
            if (typeof obj === "object") {
                return Object.entries(obj)
                    .map(([k, v]) => {
                        if (typeof v === "object") {
                            return `${pad}${k}:\n${toY(v, indent + 1)}`;
                        }
                        return `${pad}${k}: ${String(v)}\n`;
                    })
                    .join("");
            }
            return `${pad}${String(obj)}\n`;
        }
        return toY(json).trim();
    }

    const grouped = groupTypes();

    return (
        <div className="min-h-screen p-6 bg-gray-50">
            <div className="max-w-6xl mx-auto">
                <header className="flex items-center justify-between mb-6">
                    <div>
                        <h1 className="text-2xl font-bold">Automation Builder</h1>
                        <p className="text-sm text-gray-500">
                            Lightweight editor that targets your `x-block-type` metadata.
                        </p>
                    </div>
                    <div className="space-x-2">
                        <button className="px-3 py-2 bg-blue-600 text-white rounded" onClick={() => openModal()}>
                            Add Block
                        </button>
                        <button
                            className="px-3 py-2 bg-green-600 text-white rounded"
                            onClick={() => {
                                const out = exportJson();
                                navigator.clipboard?.writeText(out);
                                alert("JSON copied to clipboard");
                            }}>
                            Copy JSON
                        </button>
                        <button
                            className="px-3 py-2 bg-yellow-500 text-black rounded"
                            onClick={() => {
                                const y = exportYaml();
                                navigator.clipboard?.writeText(y);
                                alert("YAML copied to clipboard");
                            }}>
                            Copy YAML
                        </button>
                    </div>
                </header>

                <main className="grid grid-cols-4 gap-6">
                    <aside className="col-span-1 bg-white rounded shadow p-4">
                        <h3 className="font-semibold mb-3">Type Palette</h3>
                        {loading && <div>Loading types...</div>}
                        {error && <div className="text-red-500">{error}</div>}
                        {!loading && !error && (
                            <div className="space-y-4 max-h-[60vh] overflow-auto">
                                {[...grouped.entries()].map(([group, items]) => (
                                    <div key={group}>
                                        <div className="text-xs font-semibold text-gray-600 uppercase">{group}</div>
                                        <div className="mt-2 space-y-2">
                                            {items.map((it) => (
                                                <div
                                                    key={it.name}
                                                    className="flex items-center justify-between border rounded p-2">
                                                    <div>
                                                        <div className="font-medium">{it.label || it.name}</div>
                                                        <div className="text-xs text-gray-500">{it.description}</div>
                                                    </div>
                                                    <div className="ml-2">
                                                        <button
                                                            className="px-2 py-1 bg-blue-500 text-white rounded text-xs"
                                                            onClick={() => addBlock(it.name)}>
                                                            Add
                                                        </button>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </aside>

                    <section className="col-span-2 bg-white rounded shadow p-4">
                        <h3 className="font-semibold mb-3">Canvas</h3>
                        {canvas.length === 0 && (
                            <div className="text-gray-400">
                                No blocks yet — click Add Block or choose from the palette.
                            </div>
                        )}
                        <div className="space-y-3">
                            {canvas.map((b) => (
                                <div
                                    key={b.id}
                                    className={`border rounded p-3 ${
                                        editingBlockId === b.id ? "ring-2 ring-blue-300" : ""
                                    }`}>
                                    <div className="flex items-center justify-between">
                                        <div>
                                            <div className="font-medium">
                                                {b.typeName}{" "}
                                                <span className="text-xs text-gray-500">({b.blockType})</span>
                                            </div>
                                            <div className="text-xs text-gray-500">id: {b.id}</div>
                                        </div>
                                        <div className="space-x-2">
                                            <button
                                                className="px-2 py-1 bg-gray-200 rounded"
                                                onClick={() => setEditingBlockId(b.id)}>
                                                Edit
                                            </button>
                                            <button
                                                className="px-2 py-1 bg-red-100 rounded"
                                                onClick={() => removeBlock(b.id)}>
                                                Delete
                                            </button>
                                        </div>
                                    </div>
                                    {editingBlockId === b.id && (
                                        <div className="mt-3 border-t pt-3">{renderEditorFor(b)}</div>
                                    )}
                                </div>
                            ))}
                        </div>
                    </section>

                    <aside className="col-span-1 bg-white rounded shadow p-4">
                        <h3 className="font-semibold mb-3">Preview / Export</h3>
                        <div className="text-xs text-gray-500 mb-2">JSON</div>
                        <pre className="text-xs bg-gray-100 rounded p-2 max-h-40 overflow-auto">{exportJson()}</pre>
                        <div className="text-xs text-gray-500 my-2">YAML</div>
                        <pre className="text-xs bg-gray-100 rounded p-2 max-h-40 overflow-auto">{exportYaml()}</pre>
                    </aside>
                </main>
            </div>

            {/* Modal: quick chooser by group */}
            {isModalOpen && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
                    <div className="bg-white rounded shadow-lg w-3/4 max-w-3xl p-4">
                        <div className="flex items-center justify-between mb-3">
                            <h2 className="text-lg font-semibold">Choose a Block</h2>
                            <button className="text-gray-600" onClick={() => setIsModalOpen(false)}>
                                Close
                            </button>
                        </div>
                        <div className="grid grid-cols-3 gap-3 max-h-[60vh] overflow-auto">
                            {types.map((t) => (
                                <div key={t.name} className="border rounded p-3">
                                    <div className="font-medium">{t.label || t.name}</div>
                                    <div className="text-xs text-gray-500 mb-2">{t.description}</div>
                                    <div className="text-xs text-gray-400 mb-2">schema: {t.schema?.type || "-"}</div>
                                    <div className="flex justify-end">
                                        <button
                                            className="px-2 py-1 bg-blue-600 text-white rounded text-sm"
                                            onClick={() => addBlock(t.name)}>
                                            Add
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
