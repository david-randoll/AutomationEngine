"use client";
import React from "react";
import { AutomationProvider } from "./AutomationContext";
import { useForm, FormProvider } from "react-hook-form";

const Contexts = ({ children }: Readonly<{ children: React.ReactNode }>) => {
    const methods = useForm({
        defaultValues: {
            alias: "",
            description: "",
            triggers: [],
            conditions: [],
            actions: [],
            variables: [],
            results: [],
        },
    });

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
            <FormProvider {...methods}>{children}</FormProvider>
        </AutomationProvider>
    );
};

export default Contexts;
