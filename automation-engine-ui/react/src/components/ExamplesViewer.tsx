
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
                    <FaLightbulb className="h-4 w-4 mr-1" />
                    Examples
                </Button>
            </DialogTrigger>
            <DialogContent className="w-full max-w-[95vw] md:max-w-4xl">
                <DialogHeader>
                    <DialogTitle>Examples</DialogTitle>
                </DialogHeader>

                {/* Toggle Buttons */}
                <div className="mb-3 flex flex-wrap items-center gap-2">
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

                {/* Editor */}
                <div className="h-[300px] md:h-[400px] w-full rounded-md border overflow-hidden">
                    <MonacoEditor
                        height="100%"
                        width="100%"
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
