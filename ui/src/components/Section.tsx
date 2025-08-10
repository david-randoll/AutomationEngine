import React, {  } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

// Section wrapper
const Section: React.FC<{ title: string; extra?: React.ReactNode; children: React.ReactNode }> = ({
    title,
    extra,
    children,
}) => (
    <Card className="mb-6">
        <CardHeader className="flex items-center justify-between">
            <CardTitle className="text-lg font-semibold">{title}</CardTitle>
            {extra}
        </CardHeader>
        <CardContent>{children}</CardContent>
    </Card>
);

export default Section