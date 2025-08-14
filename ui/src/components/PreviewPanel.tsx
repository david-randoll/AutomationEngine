"use client";

import React from "react";
import yaml from "js-yaml";
import { useFormContext } from "react-hook-form";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

/**
 * Utility to export JSON string from form values
 */
function exportJson(data: any): string {
    try {
        return JSON.stringify(data, null, 2);
    } catch (e) {
        return `Error serializing JSON: ${(e as Error).message}`;
    }
}

/**
 * Utility to export YAML string from form values
 */
function exportYaml(data: any): string {
    try {
        return yaml.dump(data, { noRefs: true });
    } catch (e) {
        return `Error serializing YAML: ${(e as Error).message}`;
    }
}

const PreviewPanel = () => {
    // Get current form data from react-hook-form context
    const { watch } = useFormContext();
    const formData = watch();

    return (
        <Card>
            <CardHeader>
                <CardTitle className="text-lg font-semibold">Preview</CardTitle>
            </CardHeader>
            <CardContent>
                <div className="text-xs text-gray-500 mb-2">JSON</div>
                <pre className="text-xs bg-gray-100 rounded p-2 max-h-80 overflow-auto">{exportJson(formData)}</pre>

                <div className="text-xs text-gray-500 my-2">YAML</div>
                <pre className="text-xs bg-gray-100 rounded p-2 max-h-80 overflow-auto">{exportYaml(formData)}</pre>
            </CardContent>
        </Card>
    );
};

export default PreviewPanel;
