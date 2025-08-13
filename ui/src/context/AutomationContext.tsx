"use client";

import React, { createContext, useContext, useState, ReactNode } from "react";

type AutomationContextType = {
    automation: Automation;
    setAutomation: React.Dispatch<React.SetStateAction<Automation>>;
    updateModule: (path: Path, newData: any) => void;
    removeModule: (path: Path) => void;
    addModule: (path: Path, newData: ModuleType) => void;
};

const AutomationContext = createContext<AutomationContextType | undefined>(undefined);

export const useAutomation = () => {
    const ctx = useContext(AutomationContext);
    if (!ctx) throw new Error("useAutomation must be used within AutomationProvider");
    return ctx;
};

export const AutomationProvider = ({
    initialAutomation,
    children,
}: {
    initialAutomation: Automation;
    children: ReactNode;
}) => {
    const [automation, setAutomation] = useState<Automation>(initialAutomation);

    const updateModule = (path: Path, newData: any) => {
        setAutomation((prev) => {
            const updated = structuredClone(prev);
            let target: any = updated;
            for (let i = 0; i < path.length - 1; i++) target = target[path[i]];
            target[path[path.length - 1]] = newData;
            return updated;
        });
    };

    const removeModule = (path: Path) => {
        setAutomation((prev) => {
            const updated = structuredClone(prev);
            let target: any = updated;
            for (let i = 0; i < path.length - 1; i++) target = target[path[i]];
            if (Array.isArray(target)) target.splice(path[path.length - 1] as number, 1);
            else delete target[path[path.length - 1]];
            return updated;
        });
    };

    const addModule = (path: Path, newData: ModuleType) => {
        setAutomation((prev) => {
            const updated = structuredClone(prev);
            let target: any = updated;
            for (let i = 0; i < path.length; i++) target = target[path[i]];
            if (Array.isArray(target)) target.push(newData);
            else console.warn("Target for addModule is not an array", path);
            return updated;
        });
    };

    return (
        <AutomationContext.Provider value={{ automation, setAutomation, updateModule, removeModule, addModule }}>
            {children}
        </AutomationContext.Provider>
    );
};
