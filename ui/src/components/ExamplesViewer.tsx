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
    const [format, setFormat] = useState<EditMode>("json");

    return (
        <Dialog>
            <DialogTrigger asChild>
                <Button variant="outline" size="sm">
                    <FaLightbulb className="h-4 w-4" />
                    Examples
                </Button>
            </DialogTrigger>
            <DialogContent className="min-w-4xl max-w-4xl">
                <DialogHeader>
                    <DialogTitle>Examples</DialogTitle>
                </DialogHeader>

                <div className="mb-3 flex items-center gap-2">
                    <Button
                        variant={format === "json" ? "default" : "outline"}
                        size="sm"
                        className="cursor-pointer"
                        onClick={() => setFormat("json")}>
                        JSON
                    </Button>
                    <Button
                        variant={format === "yaml" ? "default" : "outline"}
                        size="sm"
                        className="cursor-pointer"
                        onClick={() => setFormat("yaml")}>
                        YAML
                    </Button>
                </div>

                <div className="h-[400px] rounded-md border">
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
                            scrollBeyondLastLine: false,
                        }}
                    />
                </div>
            </DialogContent>
        </Dialog>
    );
};

export default ExamplesViewer;
