import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

function PreviewPanel({ automation }: { automation: Automation }) {
    function exportJson() {
        return JSON.stringify(automation, null, 2);
    }

    function exportYaml() {
        function toY(obj: any, indent = 0): string {
            const pad = "  ".repeat(indent);
            if (Array.isArray(obj)) {
                if (obj.length === 0) return "[]\n";
                return (
                    obj
                        .map((v) => `${pad}- ${typeof v === "object" ? "\n" + toY(v, indent + 1) : String(v) + "\n"}`)
                        .join("") + (indent === 0 ? "" : "")
                );
            }
            if (obj === null) return "null\n";
            if (typeof obj === "object") {
                return Object.entries(obj)
                    .map(([k, v]) =>
                        typeof v === "object" ? `${pad}${k}:\n${toY(v, indent + 1)}` : `${pad}${k}: ${String(v)}\n`
                    )
                    .join("");
            }
            return `${pad}${String(obj)}\n`;
        }
        return toY(automation).trim();
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle className="text-lg font-semibold">Preview</CardTitle>
            </CardHeader>
            <CardContent>
                <div className="text-xs text-gray-500 mb-2">JSON</div>
                <pre className="text-xs bg-gray-100 rounded p-2 max-h-40 overflow-auto">{exportJson()}</pre>
                <div className="text-xs text-gray-500 my-2">YAML</div>
                <pre className="text-xs bg-gray-100 rounded p-2 max-h-40 overflow-auto">{exportYaml()}</pre>
            </CardContent>
        </Card>
    );
}

export default PreviewPanel;
