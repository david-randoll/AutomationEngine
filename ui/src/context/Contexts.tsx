"use client";
import React from "react";
import { AutomationProvider } from "./AutomationContext";

const Contexts = ({ children }: Readonly<{ children: React.ReactNode }>) => {
    return <AutomationProvider>{children}</AutomationProvider>;
};

export default Contexts;
