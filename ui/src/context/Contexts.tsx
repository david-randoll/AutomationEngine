"use client";

import React from "react";
import { useForm, FormProvider } from "react-hook-form";

const defaultValues = {
    alias: "",
    description: "",
    variables: [],
    triggers: [],
    conditions: [],
    actions: [],
    result: [],
};

const Contexts = ({ children }: { children: React.ReactNode }) => {
    const methods = useForm({
        defaultValues,
        mode: "all", // optional: enables validation and updates on change
    });

    return <FormProvider {...methods}>{children}</FormProvider>;
};

export default Contexts;
