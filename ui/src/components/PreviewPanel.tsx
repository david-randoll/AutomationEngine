import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { exportJson, exportYaml } from "@/utils/automation";

interface PreviewPanelProps {
    automation: Automation;
}

const PreviewPanel = ({ automation }: PreviewPanelProps) => {
    return (
        <Card>
            <CardHeader>
                <CardTitle className="text-lg font-semibold">Preview</CardTitle>
            </CardHeader>
            <CardContent>
                <div className="text-xs text-gray-500 mb-2">JSON</div>
                <pre className="text-xs bg-gray-100 rounded p-2 max-h-80 overflow-auto">{exportJson(automation)}</pre>
                <div className="text-xs text-gray-500 my-2">YAML</div>
                <pre className="text-xs bg-gray-100 rounded p-2 max-h-80 overflow-auto">{exportYaml(automation)}</pre>
            </CardContent>
        </Card>
    );
};

export default PreviewPanel;
