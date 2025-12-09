import { createContext, useContext, useState, useMemo, type ReactNode } from "react";
import type { JsonSchema } from "@/types/types";

interface AutomationEngineContextType {
    getSchema: (path: string, loader: () => Promise<JsonSchema | null | unknown>) => Promise<JsonSchema | null | unknown>;
    setSchema: (path: string, schema: JsonSchema) => void;
    evictSchema: (path: string) => void;
    hasSchema: (path: string) => boolean;
    isLoading: (path: string) => boolean;
}

const AutomationEngineContext = createContext<AutomationEngineContextType | null>(null);

export const AutomationEngineProvider = ({ children }: { children: ReactNode }) => {
    const [schemas, setSchemas] = useState<Map<string, JsonSchema>>(new Map());
    const [loadingPaths, setLoadingPaths] = useState<Set<string>>(new Set());

    const hasSchema = (path: string) => schemas.has(path);
    const isLoading = (path: string) => loadingPaths.has(path);

    const shouldCacheSchema = false;

    const getSchema = async (path: string, loader: () => Promise<JsonSchema | null | unknown>) => {
        if (hasSchema(path) && shouldCacheSchema) {
            return schemas.get(path)!;
        }

        setLoadingPaths((prev) => new Set(prev).add(path));

        try {
            const schema = await loader();
            if (schema) {
                setSchemas((prev) => new Map(prev.set(path, schema as JsonSchema)));
            }
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
        const newCache = new Map(schemas);
        newCache.delete(path);
        setSchemas(newCache);
    };

    const contextValue = useMemo(
        () => ({ getSchema, setSchema, hasSchema, evictSchema, isLoading }),
        // eslint-disable-next-line react-hooks/exhaustive-deps
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
