"use client";

import React, { createContext, useContext, useState, ReactNode } from "react";

interface AutomationEngineContextType {
    getSchema: (path: string, loader: () => Promise<JsonSchema>) => Promise<JsonSchema>;
    setSchema: (path: string, schema: JsonSchema) => void;
    hasSchema: (path: string) => boolean;
}

const AutomationEngineContext = createContext<AutomationEngineContextType | null>(null);

export const AutomationEngineProvider = ({ children }: { children: ReactNode }) => {
    const [cache, setCache] = useState<Map<string, JsonSchema>>(new Map());

    const getSchema = async (path: string, loader: () => Promise<JsonSchema>) => {
        if (cache.has(path)) {
            return cache.get(path)!;
        }
        const schema = await loader();
        setCache(new Map(cache.set(path, schema)));
        return schema;
    };

    const setSchema = (path: string, schema: JsonSchema) => {
        setCache(new Map(cache.set(path, schema)));
    };

    const hasSchema = (path: string) => cache.has(path);

    const contextValue = React.useMemo(
        () => ({ getSchema, setSchema, hasSchema }),
        [getSchema, setSchema, hasSchema]
    );

    return (
        <AutomationEngineContext.Provider value={contextValue}>
            {children}
        </AutomationEngineContext.Provider>
    );
};

export const useAutomationEngine = () => {
    const ctx = useContext(AutomationEngineContext);
    if (!ctx) {
        throw new Error("useAutomationEngine must be used within an AutomationEngineProvider");
    }
    return ctx;
};
