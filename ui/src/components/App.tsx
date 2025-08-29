"use client";

import { AutomationEngineProvider } from "@/providers/AutomationEngineProvider";
import React from "react";
import { useForm, FormProvider } from "react-hook-form";
import { Toaster } from "sonner";

const defaultValues = {};

const App = ({ children }: { children: React.ReactNode }) => {
    const methods = useForm({ defaultValues });

    return (
        <AutomationEngineProvider>
            <FormProvider {...methods}>{children}</FormProvider>
            <Toaster />
        </AutomationEngineProvider>
    );
};

export default App;
