"use client";
import AutomationBuilderPage from "@/components/AutomationBuilderPage";
import React, { useEffect, useState } from "react";

export default function AutomationApp() {
    const [automationSchema, setAutomationSchema] = useState<ModuleType | null>(null);

    useEffect(() => {
        fetch("http://localhost:8085/automation-engine/automation-definition/schema")
            .then((res) => res.json())
            .then(setAutomationSchema)
            .catch(console.error);
    }, []);

    if (!automationSchema) return <div>Loading schema...</div>;

    return <AutomationBuilderPage automationSchema={automationSchema} />;
}
