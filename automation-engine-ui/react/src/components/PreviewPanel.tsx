import { useEffect, useState } from "react";
import { useWatch } from "react-hook-form";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { exportJson, exportYaml } from "@/utils/automation";
import CopyableBlock from "./CopyableBlock";
import type { Path } from "@/types/types";

interface PreviewPanelProps {
    path: Path;
}

const PreviewPanel = ({ path }: PreviewPanelProps) => {
    const formData = useWatch({ name: path.join(".") });

    const [jsonData, setJsonData] = useState("");
    const [yamlData, setYamlData] = useState("");

    useEffect(() => {
        setJsonData(exportJson(formData));
        setYamlData(exportYaml(formData));
    }, [formData]);

    return (
        <Card className="flex flex-col h-full">
            <CardHeader>
                <CardTitle className="text-lg font-semibold">Preview</CardTitle>
            </CardHeader>

            <CardContent className="flex-1 flex flex-col gap-2 overflow-hidden">
                <div className="flex-1 min-h-0 overflow-auto">
                    <CopyableBlock label="JSON" content={jsonData} language="json" />
                </div>

                <div className="flex-1 min-h-0 overflow-auto">
                    <CopyableBlock label="YAML" content={yamlData} language="yaml" />
                </div>
            </CardContent>
        </Card>
    );
};

export default PreviewPanel;
