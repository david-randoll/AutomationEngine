import { useState, useCallback, useRef, useEffect } from "react";
import MonacoEditor from "@monaco-editor/react";
import yaml from "js-yaml";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import TraceCanvas from "./TraceCanvas";
import { playgroundApi } from "@/lib/playground-api";
import type { ExecutionTrace, AutomationFormat } from "@/types/trace";
import { FaPlay, FaEye, FaCopy, FaCheck, FaExclamationTriangle, FaCode, FaGripVertical, FaListAlt } from "react-icons/fa";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import LogsViewer from "./LogsViewer";

const DEFAULT_AUTOMATION = `alias: Hello World Example
# Tracing is auto-enabled for playground
triggers:
  - trigger: always

variables:
  - variable: expression
    alias: greeting
    expression: "{{ 'Hello, ' ~ (data.name | default('World')) ~ '!' }}"

actions:
  - action: logger
    message: "{{ greeting }}"

result:
  result: basic
  value: "{{ greeting }}"
`;

const DEFAULT_INPUTS = `{
  "name": "Playground User"
}`;

const DEFAULT_TRACE_JSON = `{
  "executionId": "example-id",
  "alias": "Example Trace",
  "startedAt": 1700000000000,
  "finishedAt": 1700000000050,
  "trace": {
    "triggers": [
      {
        "type": "always",
        "alias": "Always True",
        "startedAt": 1700000000010,
        "finishedAt": 1700000000015,
        "activated": true
      }
    ],
    "actions": [
      {
        "type": "logger",
        "alias": "Log Message",
        "startedAt": 1700000000020,
        "finishedAt": 1700000000030
      }
    ],
    "result": {
      "type": "basic",
      "startedAt": 1700000000040,
      "finishedAt": 1700000000050,
      "result": "Hello, World!"
    }
  }
}`;

interface PlaygroundPageProps {
    className?: string;
}

export default function PlaygroundPage({ className }: PlaygroundPageProps) {
    // Mode: execute automation or view trace JSON
    const [mode, setMode] = useState<"execute" | "trace">("execute");

    // Execute mode state
    const [automation, setAutomation] = useState(DEFAULT_AUTOMATION);
    const [inputs, setInputs] = useState(DEFAULT_INPUTS);
    const [format, setFormat] = useState<AutomationFormat>("YAML");

    // Trace JSON mode state
    const [traceJsonInput, setTraceJsonInput] = useState(DEFAULT_TRACE_JSON);

    // Execution result state
    const [trace, setTrace] = useState<ExecutionTrace | null>(null);
    const [result, setResult] = useState<unknown>(null);
    const [executed, setExecuted] = useState<boolean | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const [copied, setCopied] = useState(false);
    const [logsModalOpen, setLogsModalOpen] = useState(false);
    const [leftPanelWidth, setLeftPanelWidth] = useState(450);
    const [rightPanelWidth, setRightPanelWidth] = useState(350);
    const containerRef = useRef<HTMLDivElement>(null);
    const isResizingLeft = useRef(false);
    const isResizingRight = useRef(false);

    // Convert automation between formats
    const handleFormatChange = useCallback((newFormat: AutomationFormat) => {
        if (newFormat === format) return;

        try {
            if (newFormat === "JSON") {
                // Convert YAML to JSON
                const parsed = yaml.load(automation);
                setAutomation(JSON.stringify(parsed, null, 2));
            } else {
                // Convert JSON to YAML
                const parsed = JSON.parse(automation);
                setAutomation(yaml.dump(parsed, { indent: 2, lineWidth: -1 }));
            }
            setFormat(newFormat);
        } catch (e) {
            setError(`Failed to convert: ${(e as Error).message}`);
        }
    }, [automation, format]);

    // Execute the automation
    const handleExecute = useCallback(async () => {
        setLoading(true);
        setError(null);
        setTrace(null);
        setResult(null);
        setExecuted(null);

        try {
            // Parse inputs
            let parsedInputs: Record<string, unknown> = {};
            if (inputs.trim()) {
                try {
                    parsedInputs = JSON.parse(inputs);
                } catch {
                    // Try YAML
                    parsedInputs = yaml.load(inputs) as Record<string, unknown>;
                }
            }

            const response = await playgroundApi.execute({
                automation,
                format,
                inputs: parsedInputs,
            });

            setExecuted(response.executed);
            setResult(response.result);
            setTrace(response.trace || null);
            if (response.error) {
                setError(response.error);
            }
        } catch (e) {
            setError((e as Error).message);
        } finally {
            setLoading(false);
        }
    }, [automation, inputs, format]);

    // Parse trace JSON for visualization
    const handleRenderTrace = useCallback(() => {
        setError(null);
        try {
            const parsed = JSON.parse(traceJsonInput) as ExecutionTrace;
            setTrace(parsed);
            setExecuted(null);
            setResult(null);
        } catch (e) {
            setError(`Invalid trace JSON: ${(e as Error).message}`);
        }
    }, [traceJsonInput]);

    // Copy trace to clipboard
    const copyTrace = useCallback(async () => {
        if (trace) {
            await navigator.clipboard.writeText(JSON.stringify(trace, null, 2));
            setCopied(true);
            setTimeout(() => setCopied(false), 2000);
        }
    }, [trace]);

    // Computed values
    const hasTrace = !!trace;

    // Resizing handlers
    useEffect(() => {
        const handleMouseMove = (e: MouseEvent) => {
            if (!containerRef.current) return;

            if (isResizingLeft.current) {
                const containerRect = containerRef.current.getBoundingClientRect();
                const newWidth = e.clientX - containerRect.left;
                if (newWidth >= 300 && newWidth <= 700) {
                    setLeftPanelWidth(newWidth);
                }
            }

            if (isResizingRight.current) {
                const containerRect = containerRef.current.getBoundingClientRect();
                const newWidth = containerRect.right - e.clientX;
                if (newWidth >= 300 && newWidth <= 700) {
                    setRightPanelWidth(newWidth);
                }
            }
        };

        const handleMouseUp = () => {
            isResizingLeft.current = false;
            isResizingRight.current = false;
            document.body.style.cursor = "";
            document.body.style.userSelect = "";
        };

        document.addEventListener("mousemove", handleMouseMove);
        document.addEventListener("mouseup", handleMouseUp);

        return () => {
            document.removeEventListener("mousemove", handleMouseMove);
            document.removeEventListener("mouseup", handleMouseUp);
        };
    }, []);

    const handleLeftResizeStart = () => {
        isResizingLeft.current = true;
        document.body.style.cursor = "col-resize";
        document.body.style.userSelect = "none";
    };

    const handleRightResizeStart = () => {
        isResizingRight.current = true;
        document.body.style.cursor = "col-resize";
        document.body.style.userSelect = "none";
    };

    return (
        <div className={`flex flex-col h-full ${className}`}>
            {/* Header with Mode Tabs */}
            <div className="flex items-center justify-between border-b px-4 py-2 bg-white shrink-0">
                <div className="flex items-center gap-4">
                    <h1 className="text-lg font-semibold">Playground</h1>
                    <div className="flex bg-muted rounded-lg p-0.5">
                        <button
                            onClick={() => setMode("execute")}
                            className={`px-3 py-1.5 text-sm font-medium rounded-md flex items-center gap-2 transition-colors ${mode === "execute"
                                ? "bg-background text-foreground shadow-sm"
                                : "text-muted-foreground hover:text-foreground"
                                }`}
                        >
                            <FaPlay className="w-3 h-3" />
                            Execute
                        </button>
                        <button
                            onClick={() => setMode("trace")}
                            className={`px-3 py-1.5 text-sm font-medium rounded-md flex items-center gap-2 transition-colors ${mode === "trace"
                                ? "bg-background text-foreground shadow-sm"
                                : "text-muted-foreground hover:text-foreground"
                                }`}
                        >
                            <FaEye className="w-3 h-3" />
                            View Trace
                        </button>
                    </div>
                </div>
                {hasTrace && (
                    <div className="flex gap-2">
                        {trace.logs && trace.logs.length > 0 && (
                            <Button
                                variant="outline"
                                size="sm"
                                onClick={() => setLogsModalOpen(true)}
                                className="gap-2"
                            >
                                <FaListAlt className="w-3 h-3" />
                                View Logs ({trace.logs.length})
                            </Button>
                        )}
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={copyTrace}
                            className="gap-2"
                        >
                            {copied ? (
                                <>
                                    <FaCheck className="w-3 h-3" />
                                    Copied
                                </>
                            ) : (
                                <>
                                    <FaCopy className="w-3 h-3" />
                                    Copy Trace
                                </>
                            )}
                        </Button>
                    </div>
                )}
            </div>

            {/* Main Content */}
            <div ref={containerRef} className="flex-1 flex overflow-hidden min-h-0">
                {/* Left Panel - Inputs */}
                <div style={{ width: `${leftPanelWidth}px` }} className="border-r flex flex-col bg-white shrink-0 relative">{mode === "execute" ? (
                    <>
                        {/* Automation Editor */}
                        <div className="flex-1 flex flex-col min-h-0 border-b">
                            <div className="flex items-center justify-between px-3 py-2 border-b bg-gray-50 shrink-0">
                                <span className="text-sm font-medium text-gray-700">
                                    Automation
                                </span>
                                <div className="flex bg-gray-200 rounded p-0.5">
                                    <button
                                        onClick={() => handleFormatChange("YAML")}
                                        className={`px-2 py-0.5 text-xs font-medium rounded transition-colors ${format === "YAML"
                                            ? "bg-white text-gray-900 shadow-sm"
                                            : "text-gray-500 hover:text-gray-700"
                                            }`}
                                    >
                                        YAML
                                    </button>
                                    <button
                                        onClick={() => handleFormatChange("JSON")}
                                        className={`px-2 py-0.5 text-xs font-medium rounded transition-colors ${format === "JSON"
                                            ? "bg-white text-gray-900 shadow-sm"
                                            : "text-gray-500 hover:text-gray-700"
                                            }`}
                                    >
                                        JSON
                                    </button>
                                </div>
                            </div>
                            <div className="flex-1 min-h-0">
                                <MonacoEditor
                                    height="100%"
                                    language={format === "JSON" ? "json" : "yaml"}
                                    value={automation}
                                    onChange={(value) => setAutomation(value || "")}
                                    options={{
                                        minimap: { enabled: false },
                                        fontSize: 13,
                                        lineNumbers: "on",
                                        scrollBeyondLastLine: false,
                                        automaticLayout: true,
                                        tabSize: 2,
                                        wordWrap: "on",
                                    }}
                                />
                            </div>
                        </div>

                        {/* Inputs Editor */}
                        <div className="h-[180px] flex flex-col shrink-0">
                            <div className="flex items-center justify-between px-3 py-2 border-b bg-gray-50 shrink-0">
                                <span className="text-sm font-medium text-gray-700">
                                    Event Inputs (JSON)
                                </span>
                            </div>
                            <div className="flex-1 min-h-0">
                                <MonacoEditor
                                    height="100%"
                                    language="json"
                                    value={inputs}
                                    onChange={(value) => setInputs(value || "{}")}
                                    options={{
                                        minimap: { enabled: false },
                                        fontSize: 13,
                                        lineNumbers: "on",
                                        scrollBeyondLastLine: false,
                                        automaticLayout: true,
                                        tabSize: 2,
                                    }}
                                />
                            </div>
                        </div>

                        {/* Execute Button */}
                        <div className="px-4 py-3 border-t bg-gray-50 shrink-0">
                            <Button
                                onClick={handleExecute}
                                disabled={loading}
                                className="w-full gap-2"
                                size="lg"
                            >
                                <FaPlay className="w-4 h-4" />
                                {loading ? "Executing..." : "Execute"}
                            </Button>
                        </div>
                    </>
                ) : (
                    <>
                        {/* Trace JSON Editor */}
                        <div className="flex-1 flex flex-col min-h-0">
                            <div className="flex items-center justify-between px-3 py-2 border-b bg-gray-50 shrink-0">
                                <span className="text-sm font-medium text-gray-700">
                                    Trace JSON
                                </span>
                            </div>
                            <div className="flex-1 min-h-0">
                                <MonacoEditor
                                    height="100%"
                                    language="json"
                                    value={traceJsonInput}
                                    onChange={(value) => setTraceJsonInput(value || "{}")}
                                    options={{
                                        minimap: { enabled: false },
                                        fontSize: 13,
                                        lineNumbers: "on",
                                        scrollBeyondLastLine: false,
                                        automaticLayout: true,
                                        tabSize: 2,
                                        wordWrap: "on",
                                    }}
                                />
                            </div>
                        </div>

                        {/* Render Button */}
                        <div className="px-4 py-3 border-t bg-gray-50 shrink-0">
                            <Button
                                onClick={handleRenderTrace}
                                className="w-full gap-2"
                                size="lg"
                            >
                                <FaEye className="w-4 h-4" />
                                Render Trace
                            </Button>
                        </div>
                    </>
                )}
                    {/* Left resize handle */}
                    <div
                        onMouseDown={handleLeftResizeStart}
                        className="absolute top-0 right-0 w-1 h-full cursor-col-resize hover:bg-blue-400 transition-colors group"
                    >
                        <div className="absolute top-1/2 right-0 -translate-y-1/2 translate-x-1/2 opacity-0 group-hover:opacity-100 transition-opacity">
                            <FaGripVertical className="w-3 h-3 text-gray-400" />
                        </div>
                    </div>
                </div>

                {/* Right Panel - Trace Visualization */}
                <div className="flex-1 flex flex-col overflow-hidden min-h-0">
                    {/* Error Banner */}
                    {error && (
                        <div className="px-4 py-3 bg-red-50 border-b border-red-200 flex items-center gap-2 text-red-700 shrink-0">
                            <FaExclamationTriangle className="w-4 h-4 flex-shrink-0" />
                            <span className="text-sm">{error}</span>
                        </div>
                    )}

                    {/* Execution Result Banner */}
                    {executed !== null && !error && (
                        <div
                            className={`px-4 py-3 border-b flex items-center gap-4 shrink-0 ${executed
                                ? "bg-green-50 border-green-200"
                                : "bg-yellow-50 border-yellow-200"
                                }`}
                        >
                            <div className="flex items-center gap-2">
                                {executed ? (
                                    <FaCheck className="w-4 h-4 text-green-600" />
                                ) : (
                                    <FaExclamationTriangle className="w-4 h-4 text-yellow-600" />
                                )}
                                <span
                                    className={`text-sm font-medium ${executed ? "text-green-700" : "text-yellow-700"
                                        }`}
                                >
                                    {executed
                                        ? "Executed Successfully"
                                        : "Skipped (conditions not met)"}
                                </span>
                            </div>
                            {result !== undefined && result !== null && (
                                <div className="flex items-center gap-2 text-sm text-gray-600">
                                    <span>Result:</span>
                                    <code className="bg-white px-2 py-0.5 rounded border text-xs">
                                        {typeof result === "string"
                                            ? result
                                            : JSON.stringify(result)}
                                    </code>
                                </div>
                            )}
                        </div>
                    )}

                    {/* Trace Canvas */}
                    <div className="flex-1 min-h-0">
                        {hasTrace ? (
                            <TraceCanvas
                                trace={trace}
                                className="h-full"
                                rightPanelWidth={rightPanelWidth}
                                onRightPanelWidthChange={setRightPanelWidth}
                                onRightResizeStart={handleRightResizeStart}
                            />
                        ) : (
                            <div className="h-full flex items-center justify-center bg-gray-50">
                                <Card className="w-[400px]">
                                    <CardHeader>
                                        <CardTitle className="flex items-center gap-2">
                                            <FaCode className="w-5 h-5 text-gray-400" />
                                            No Trace Available
                                        </CardTitle>
                                        <CardDescription>
                                            {mode === "execute"
                                                ? "Execute an automation to see the trace visualization."
                                                : "Paste a trace JSON and click 'Render Trace' to visualize."}
                                        </CardDescription>
                                    </CardHeader>
                                    <CardContent>
                                        <ul className="text-sm text-gray-500 space-y-2">
                                            <li>• Each step shows timing and state changes</li>
                                            <li>• Click nodes to see before/after snapshots</li>
                                            <li>• Triggers and conditions show activation status</li>
                                        </ul>
                                    </CardContent>
                                </Card>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Logs Modal */}
            <Dialog open={logsModalOpen} onOpenChange={setLogsModalOpen}>
                <DialogContent className="max-w-[90vw] w-[90vw] sm:max-w-none max-h-[90vh] h-[90vh] flex flex-col p-0 gap-0">
                    <DialogHeader className="shrink-0 px-6 pt-6 pb-4 border-b">
                        <DialogTitle className="text-lg">All Execution Logs</DialogTitle>
                    </DialogHeader>
                    <div className="flex-1 min-h-0 px-6 pb-6 pt-4">
                        <LogsViewer 
                            logs={trace?.logs} 
                            title=""
                            maxHeight="100%"
                            className="h-full"
                        />
                    </div>
                </DialogContent>
            </Dialog>
        </div>
    );
}
