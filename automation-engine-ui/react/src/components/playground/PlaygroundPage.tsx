import { useState, useCallback } from "react";
import MonacoEditor from "@monaco-editor/react";
import yaml from "js-yaml";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import TraceCanvas from "./TraceCanvas";
import { playgroundApi } from "@/lib/playground-api";
import type { ExecutionTrace, AutomationFormat } from "@/types/trace";
import { FaPlay, FaCode, FaEye, FaCopy, FaCheck, FaExclamationTriangle } from "react-icons/fa";

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

    // Trace JSON mode state
    const [traceJsonInput, setTraceJsonInput] = useState(DEFAULT_TRACE_JSON);

    // Execution result state
    const [trace, setTrace] = useState<ExecutionTrace | null>(null);
    const [result, setResult] = useState<unknown>(null);
    const [executed, setExecuted] = useState<boolean | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const [copied, setCopied] = useState(false);

    // Detect format from automation text
    const detectFormat = useCallback((text: string): AutomationFormat => {
        const trimmed = text.trim();
        if (trimmed.startsWith("{")) return "JSON";
        return "YAML";
    }, []);

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

            const detectedFormat = detectFormat(automation);
            const response = await playgroundApi.execute({
                automation,
                format: detectedFormat,
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
    }, [automation, inputs, detectFormat]);

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

    return (
        <div className={`flex flex-col h-full ${className}`}>
            {/* Header with Mode Tabs */}
            <div className="flex items-center justify-between border-b px-4 py-2 bg-white shrink-0">
                <div className="flex items-center gap-4">
                    <h1 className="text-lg font-semibold">Playground</h1>
                    <div className="flex bg-muted rounded-lg p-0.5">
                        <button
                            onClick={() => setMode("execute")}
                            className={`px-3 py-1.5 text-sm font-medium rounded-md flex items-center gap-2 transition-colors ${
                                mode === "execute"
                                    ? "bg-background text-foreground shadow-sm"
                                    : "text-muted-foreground hover:text-foreground"
                            }`}
                        >
                            <FaPlay className="w-3 h-3" />
                            Execute
                        </button>
                        <button
                            onClick={() => setMode("trace")}
                            className={`px-3 py-1.5 text-sm font-medium rounded-md flex items-center gap-2 transition-colors ${
                                mode === "trace"
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
                )}
            </div>

            {/* Main Content */}
            <div className="flex-1 flex overflow-hidden min-h-0">
                {/* Left Panel - Inputs */}
                <div className="w-[450px] border-r flex flex-col bg-white shrink-0">
                    {mode === "execute" ? (
                        <>
                            {/* Automation Editor */}
                            <div className="flex-1 flex flex-col min-h-0 border-b">
                                <div className="flex items-center justify-between px-3 py-2 border-b bg-gray-50 shrink-0">
                                    <span className="text-sm font-medium text-gray-700">
                                        Automation (YAML/JSON)
                                    </span>
                                    <span className="text-xs text-gray-400">
                                        {detectFormat(automation)}
                                    </span>
                                </div>
                                <div className="flex-1 min-h-0">
                                    <MonacoEditor
                                        height="100%"
                                        language="yaml"
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
                                        theme="vs-dark"
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
                                        theme="vs-dark"
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
                                        theme="vs-dark"
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
                            className={`px-4 py-3 border-b flex items-center gap-4 shrink-0 ${
                                executed
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
                                    className={`text-sm font-medium ${
                                        executed ? "text-green-700" : "text-yellow-700"
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
                            <TraceCanvas trace={trace} className="h-full" />
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
        </div>
    );
}
