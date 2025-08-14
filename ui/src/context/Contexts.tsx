"use client";

import React from "react";
import { useForm, FormProvider } from "react-hook-form";

const defaultValues = {};

const Contexts = ({ children }: { children: React.ReactNode }) => {
    const methods = useForm({ defaultValues });

    return <FormProvider {...methods}>{children}</FormProvider>;
};

export default Contexts;
