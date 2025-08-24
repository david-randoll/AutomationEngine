"use client";

import React, { useEffect, useState } from "react";
import { useWatch } from "react-hook-form";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { exportJson, exportYaml } from "@/utils/automation";

interface PreviewPanelProps {
    path: (string | number)[];
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
                <div className="text-xs text-gray-500 mb-2">JSON</div>
                <pre className="text-xs bg-gray-100 rounded p-2 max-h-80 overflow-auto">{jsonData}</pre>

                <div className="text-xs text-gray-500 my-2">YAML</div>
                <pre className="text-xs bg-gray-100 rounded p-2 max-h-80 overflow-auto">{yamlData}</pre>
            </CardContent>
        </Card>
    );
};

export default PreviewPanel;
