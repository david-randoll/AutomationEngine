"use client";

import React, { useEffect, useState } from "react";
import { useWatch } from "react-hook-form";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { exportJson, exportYaml } from "@/utils/automation";
import CopyableBlock from "./CopyableBlock";

interface PreviewPanelProps {
    path: Path;
}

const PreviewPanel = ({ path }: PreviewPanelProps) => {
    const formData = useWatch({
        name: path.join("."),
    });

    const [jsonData, setJsonData] = useState("");
    const [yamlData, setYamlData] = useState("");

    useEffect(() => {
        setJsonData(exportJson(formData));
        setYamlData(exportYaml(formData));
    }, [formData]);

    return (
        <Card>
            <CardHeader>
                <CardTitle className="text-lg font-semibold">Preview</CardTitle>
            </CardHeader>
            <CardContent>
                <CopyableBlock label="JSON" content={jsonData} />
                <CopyableBlock label="YAML" content={yamlData} />
            </CardContent>
        </Card>
    );
};

export default PreviewPanel;
