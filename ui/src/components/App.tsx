"use client";

import React from "react";
import { useForm, FormProvider } from "react-hook-form";
import { Toaster } from "sonner";

const defaultValues = {};

const App = ({ children }: { children: React.ReactNode }) => {
    const methods = useForm({ defaultValues });

    return (
        <>
            <FormProvider {...methods}>{children}</FormProvider>
            <Toaster />
        </>
    );
};

export default App;
