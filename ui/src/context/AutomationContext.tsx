"use client";

import React, { createContext, useContext, useState, ReactNode } from "react";

interface AutomationContextValue {
    automation: Automation;
    setAutomation: React.Dispatch<React.SetStateAction<Automation>>;
    addModule: (area: Area, mod: ModuleType) => void; // top-level add
    removeModule: (area: Area, idx: number) => void; // top-level remove
    updateModule: (area: Area, idx: number, mod: ModuleType) => void; // top-level update
    // helpers that operate by id and support nested modules:
    updateModuleById: (id: string, next: ModuleType) => void;
    createModuleInstance: (mod: Partial<ModuleType>) => ModuleType;
    addChildModule: (parentId: string, fieldName: string, child: Partial<ModuleType>) => ModuleType;
    removeChildModule: (parentId: string, fieldName: string, childIdx: number) => void;
    editingId: string | null;
    setEditingId: React.Dispatch<React.SetStateAction<string | null>>;
}

const AutomationContext = createContext<AutomationContextValue | undefined>(undefined);

const AREAS = ["variables", "triggers", "conditions", "actions", "results"] as const;

export const AutomationProvider = ({ children }: { children: ReactNode }) => {
    function uid(prefix = "id") {
        return `${prefix}_${Math.random().toString(36).slice(2, 9)}`;
    }

    const [automation, setAutomation] = useState<Automation>({
        alias: "",
        description: "",
        variables: [],
        triggers: [],
        conditions: [],
        actions: [],
        results: [],
    });

    const [editingId, setEditingId] = useState<string | null>(null);

    function addModule(area: Area, mod: ModuleType) {
        const instance = { ...mod, id: mod.id || uid(mod.name), data: mod.data || {} };
        setAutomation((a) => ({
            ...a,
            [area + "s"]: [...(a as any)[area + "s"], instance],
        }));
        return instance;
    }

    function removeModule(area: Area, idx: number) {
        setAutomation((a) => {
            const arr = (a as any)[area + "s"] as ModuleType[];
            return { ...a, [area + "s"]: arr.filter((_, i) => i !== idx) };
        });
    }

    function updateModule(area: Area, idx: number, mod: ModuleType) {
        setAutomation((a) => {
            const copy = { ...a } as any;
            copy[area + "s"] = copy[area + "s"].slice();
            copy[area + "s"][idx] = mod;
            return copy;
        });
    }

    // --- Helpers to find module anywhere and return a path like ['actions', 2, 'data', 'then', 1]
    function findModulePathById(root: Automation, targetId: string): Path | null {
        // Search each top-level area array
        for (const areaKey of AREAS) {
            const arr = (root as any)[areaKey] as ModuleType[];
            for (let i = 0; i < arr.length; i++) {
                const path = searchModule(arr[i], [areaKey, i], targetId);
                if (path) return path;
            }
        }
        return null;
    }

    function searchModule(node: any, pathSoFar: Path, targetId: string): Path | null {
        if (!node || typeof node !== "object") return null;
        if (node.id === targetId) return pathSoFar.slice();

        const data = node.data;
        if (!data || typeof data !== "object") return null;

        for (const key of Object.keys(data)) {
            const val = data[key];
            if (Array.isArray(val)) {
                for (let i = 0; i < val.length; i++) {
                    const child = val[i];
                    // If child is an object that looks like a ModuleType (has id), search it
                    if (child && typeof child === "object") {
                        const childPath = searchModule(child, [...pathSoFar, "data", key, i], targetId);
                        if (childPath) return childPath;
                    }
                }
            } else if (val && typeof val === "object") {
                // single nested object (could be a module)
                const childPath = searchModule(val, [...pathSoFar, "data", key], targetId);
                if (childPath) return childPath;
            }
        }

        return null;
    }

    // --- Generic immutable setter given a path
    function setAtPath(rootObj: any, path: Path, value: any): any {
        if (path.length === 0) {
            return value;
        }
        const [head, ...rest] = path;
        if (typeof head === "number") {
            // rootObj is expected to be an array
            const arr = Array.isArray(rootObj) ? rootObj.slice() : [];
            if (rest.length === 0) {
                arr[head] = value;
                return arr;
            }
            arr[head] = setAtPath(arr[head], rest, value);
            return arr;
        } else {
            // head is string key
            const copy = { ...(rootObj || {}) };
            if (rest.length === 0) {
                copy[head] = value;
                return copy;
            }
            copy[head] = setAtPath(copy[head], rest, value);
            return copy;
        }
    }

    // --- Update module anywhere by id
    function updateModuleById(id: string, next: ModuleType) {
        setAutomation((prev) => {
            const path = findModulePathById(prev, id);
            if (!path) {
                console.warn("updateModuleById: module id not found", id);
                return prev;
            }
            return setAtPath(prev, path, next) as Automation;
        });
    }

    function createModuleInstance(mod: Partial<ModuleType>): ModuleType {
        return {
            id: mod.id || uid(mod.name || "module"),
            name: mod.name || "unknown",
            label: mod.label || mod.name || mod.name || "",
            description: mod.description,
            schema: mod.schema,
            data: mod.data || {},
        };
    }

    // add child module to a parent module's data[fieldName] array (creates array if missing)
    function addChildModule(parentId: string, fieldName: string, child: Partial<ModuleType>) {
        const instance = createModuleInstance(child);
        setAutomation((prev) => {
            const parentPath = findModulePathById(prev, parentId);
            if (!parentPath) {
                console.warn("addChildModule: parent id not found", parentId);
                return prev;
            }
            // target array path = [...parentPath, 'data', fieldName]
            const arrayPath: Path = [...parentPath, "data", fieldName];
            // read existing array (safely)
            // we will build the new automation by reading the existing array at that path
            // find current arr
            function getAtPath(obj: any, p: Path): any {
                if (!obj || p.length === 0) return obj;
                const [h, ...r] = p;
                if (h === undefined) return undefined;
                return getAtPath(obj[h], r);
            }
            const existingArr = getAtPath(prev, arrayPath) || [];
            const newArr = [...existingArr, instance];
            const newRoot = setAtPath(prev, arrayPath, newArr) as Automation;
            return newRoot;
        });
        return instance;
    }

    function removeChildModule(parentId: string, fieldName: string, childIdx: number) {
        setAutomation((prev) => {
            const parentPath = findModulePathById(prev, parentId);
            if (!parentPath) {
                console.warn("removeChildModule: parent id not found", parentId);
                return prev;
            }
            const arrayPath: Path = [...parentPath, "data", fieldName];
            // get existing array
            function getAtPath(obj: any, p: Path): any {
                if (!obj || p.length === 0) return obj;
                const [h, ...r] = p;
                return getAtPath(obj[h], r);
            }
            const existingArr = getAtPath(prev, arrayPath) || [];
            if (!Array.isArray(existingArr)) return prev;
            const newArr = existingArr.filter((_: any, i: number) => i !== childIdx);
            const newRoot = setAtPath(prev, arrayPath, newArr) as Automation;
            return newRoot;
        });
    }

    return (
        <AutomationContext.Provider
            value={{
                automation,
                setAutomation,
                addModule,
                removeModule,
                updateModule,
                updateModuleById,
                createModuleInstance,
                addChildModule,
                removeChildModule,
                editingId,
                setEditingId,
            }}>
            {children}
        </AutomationContext.Provider>
    );
};

export const useAutomation = () => {
    const ctx = useContext(AutomationContext);
    if (!ctx) throw new Error("useAutomation must be used within AutomationProvider");
    return ctx;
};
