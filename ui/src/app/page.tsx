"use client";
import AutomationBuilderPage from "@/components/AutomationBuilderPage";
import { agent } from "@/lib/agent";
import React, { useEffect, useState } from "react";

export default function AutomationApp() {
    const [automationSchema, setAutomationSchema] = useState<ModuleType | null>(null);

    useEffect(() => {
        agent
            .get<ModuleType>("/automation-engine/automation-definition/schema")
            .then(setAutomationSchema)
            .catch(console.error);
    }, []);

    if (!automationSchema) return <div>Loading schema...</div>;

    return <AutomationBuilderPage automationSchema={automationSchema} />;
}
