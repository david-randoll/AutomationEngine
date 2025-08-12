"use client";
import React from "react";
import { AutomationProvider } from "./AutomationContext";

const Contexts = ({ children }: Readonly<{ children: React.ReactNode }>) => {
    return (
        <AutomationProvider
            initialAutomation={{
                alias: "",
                description: "",
                triggers: [],
                conditions: [],
                actions: [],
                variables: [],
                results: [],
            }}>
            {children}
        </AutomationProvider>
    );
};

export default Contexts;
