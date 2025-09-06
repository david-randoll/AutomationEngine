"use client";

import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import MonacoEditor from "@monaco-editor/react";
import yaml from "js-yaml";
import { FaLightbulb } from "react-icons/fa";

interface ExamplesViewerProps {
    examples: any[];
}

const ExamplesViewer = ({ examples }: ExamplesViewerProps) => {
    const [format, setFormat] = useState<"json" | "yaml">("json");

    return (
        <Dialog>
            <DialogTrigger asChild>
                <Button variant="outline" size="sm">
                    <FaLightbulb />
                </Button>
            </DialogTrigger>
            <DialogContent className="max-w-3xl">
                <DialogHeader className="flex items-center justify-between">
                    <DialogTitle>Examples</DialogTitle>
                    <div className="flex gap-2">
                        <Button
                            variant={format === "json" ? "default" : "outline"}
                            size="sm"
                            onClick={() => setFormat("json")}>
                            JSON
                        </Button>
                        <Button
                            variant={format === "yaml" ? "default" : "outline"}
                            size="sm"
                            onClick={() => setFormat("yaml")}>
                            YAML
                        </Button>
                    </div>
                </DialogHeader>

                <div className="h-[400px]">
                    <MonacoEditor
                        height="100%"
                        defaultLanguage={format}
                        language={format}
                        value={format === "json" ? JSON.stringify(examples, null, 2) : yaml.dump(examples)}
                        options={{
                            readOnly: true,
                            minimap: { enabled: false },
                            tabSize: 2,
                            fontSize: 14,
                            wordWrap: "on",
                            automaticLayout: true,
                        }}
                    />
                </div>
            </DialogContent>
        </Dialog>
    );
};

export default ExamplesViewer;
