"use client";

import React, { createContext, useContext, useState, ReactNode } from "react";

interface AutomationEngineContextType {
    getSchema: (path: string, loader: () => Promise<JsonSchema>) => Promise<JsonSchema>;
    setSchema: (path: string, schema: JsonSchema) => void;
    evictSchema: (path: string) => void;
    hasSchema: (path: string) => boolean;
    isLoading: (path: string) => boolean;
}

const AutomationEngineContext = createContext<AutomationEngineContextType | null>(null);

export const AutomationEngineProvider = ({ children }: { children: ReactNode }) => {
    const [schemas, setSchemas] = useState<Map<string, JsonSchema>>(new Map());
    const [loadingPaths, setLoadingPaths] = useState<Set<string>>(new Set());

    const getSchema = async (path: string, loader: () => Promise<JsonSchema>) => {
        if (hasSchema(path)) {
            return schemas.get(path)!;
        }

        setLoadingPaths((prev) => new Set(prev).add(path));

        try {
            const schema = await loader();
            setSchemas((prev) => new Map(prev.set(path, schema)));
            return schema;
        } finally {
            setLoadingPaths((prev) => {
                const copy = new Set(prev);
                copy.delete(path);
                return copy;
            });
        }
    };

    const setSchema = (path: string, schema: JsonSchema) => {
        setSchemas((prev) => new Map(prev.set(path, schema)));
    };

    const evictSchema = (path: string) => {
        setSchemas((prev) => {
            const copy = new Map(prev);
            copy.delete(path);
            return copy;
        });
    };

    const hasSchema = (path: string) => schemas.has(path);
    const isLoading = (path: string) => loadingPaths.has(path);

    const contextValue = React.useMemo(
        () => ({ getSchema, setSchema, hasSchema, evictSchema, isLoading }),
        [schemas, loadingPaths]
    );

    return <AutomationEngineContext.Provider value={contextValue}>{children}</AutomationEngineContext.Provider>;
};

export const useAutomationEngine = () => {
    const ctx = useContext(AutomationEngineContext);
    if (!ctx) {
        throw new Error("useAutomationEngine must be used within an AutomationEngineProvider");
    }
    return ctx;
};
