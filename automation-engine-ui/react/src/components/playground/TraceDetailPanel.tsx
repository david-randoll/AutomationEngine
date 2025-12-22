import { useMemo, useState } from "react";
import { DiffEditor } from "@monaco-editor/react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import type { TraceEntry, TraceSnapshot } from "@/types/trace";
import { FaCheck, FaTimes, FaClock, FaExpand, FaExchangeAlt } from "react-icons/fa";

interface TraceDetailPanelProps {
    entry: TraceEntry | null;
    className?: string;
}

type DiffType = "event" | "context";

/**
 * Get JSON string for snapshot data
 */
function getSnapshotJson(snapshot: TraceSnapshot | undefined, key: "eventSnapshot" | "contextSnapshot"): string {
    if (!snapshot || !snapshot[key] || Object.keys(snapshot[key]!).length === 0) {
        return "{}";
    }
    return JSON.stringify(snapshot[key], null, 2);
}

/**
 * Panel component for displaying detailed information about a selected trace entry.
 * Shows a GitHub-style diff view comparing before and after snapshots.
 */
export default function TraceDetailPanel({ entry, className }: TraceDetailPanelProps) {
    const [diffType, setDiffType] = useState<DiffType>("event");
    const [fullscreenOpen, setFullscreenOpen] = useState(false);

    const duration = useMemo(() => {
        if (!entry?.startedAt || !entry?.finishedAt) return null;
        return entry.finishedAt - entry.startedAt;
    }, [entry?.startedAt, entry?.finishedAt]);

    // Compute diff content
    const { beforeContent, afterContent, hasEventData, hasContextData } = useMemo(() => {
        if (!entry) {
            return { beforeContent: "{}", afterContent: "{}", hasEventData: false, hasContextData: false };
        }

        const beforeEvent = getSnapshotJson(entry.before, "eventSnapshot");
        const afterEvent = getSnapshotJson(entry.after, "eventSnapshot");
        const beforeContext = getSnapshotJson(entry.before, "contextSnapshot");
        const afterContext = getSnapshotJson(entry.after, "contextSnapshot");

        return {
            beforeContent: diffType === "event" ? beforeEvent : beforeContext,
            afterContent: diffType === "event" ? afterEvent : afterContext,
            hasEventData: beforeEvent !== "{}" || afterEvent !== "{}",
            hasContextData: beforeContext !== "{}" || afterContext !== "{}",
        };
    }, [entry, diffType]);

    if (!entry) {
        return (
            <Card className={className}>
                <CardContent className="flex items-center justify-center h-full min-h-[200px]">
                    <p className="text-gray-400 text-sm">
                        Select a node to view details
                    </p>
                </CardContent>
            </Card>
        );
    }

    // Determine status for triggers and conditions
    const hasStatus = entry.category === "trigger" || entry.category === "condition";
    const isSuccess =
        (entry.category === "trigger" && (entry as TraceEntry & { activated?: boolean }).activated) ||
        (entry.category === "condition" && (entry as TraceEntry & { satisfied?: boolean }).satisfied);

    // For result entries, show the result value instead of diff
    const isResultEntry = entry.category === "result";
    const resultValue = isResultEntry ? (entry as TraceEntry & { result?: unknown }).result : null;

    return (
        <>
            <Card className={`${className} flex flex-col`}>
                <CardHeader className="pb-3 shrink-0">
                    <div className="flex items-center justify-between">
                        <CardTitle className="text-base">
                            {entry.alias || entry.type || "Unknown"}
                        </CardTitle>
                        {hasStatus && (
                            <span
                                className={`flex items-center gap-1 text-xs px-2 py-1 rounded-full ${isSuccess
                                    ? "bg-green-100 text-green-700"
                                    : "bg-red-100 text-red-700"
                                    }`}
                            >
                                {isSuccess ? (
                                    <>
                                        <FaCheck className="w-3 h-3" />
                                        {entry.category === "trigger" ? "Activated" : "Satisfied"}
                                    </>
                                ) : (
                                    <>
                                        <FaTimes className="w-3 h-3" />
                                        {entry.category === "trigger" ? "Not Activated" : "Not Satisfied"}
                                    </>
                                )}
                            </span>
                        )}
                    </div>
                    <div className="flex items-center gap-4 text-xs text-gray-500 mt-1">
                        <span className="uppercase font-medium">{entry.category}</span>
                        {entry.type && entry.alias && (
                            <span className="text-gray-400">Type: {entry.type}</span>
                        )}
                        {duration !== null && (
                            <span className="flex items-center gap-1">
                                <FaClock className="w-3 h-3" />
                                {duration < 1 ? "<1ms" : `${duration}ms`}
                            </span>
                        )}
                    </div>
                </CardHeader>
                <CardContent className="pt-0 flex-1 flex flex-col min-h-0">
                    {isResultEntry ? (
                        // Show result value for result entries
                        <div className="flex-1 flex flex-col min-h-0">
                            <div className="flex items-center justify-between mb-2 shrink-0">
                                <h4 className="text-xs font-semibold text-gray-500 uppercase">
                                    Result Value
                                </h4>
                            </div>
                            <pre className="flex-1 text-xs bg-gray-900 text-gray-100 p-3 rounded-lg overflow-auto font-mono">
                                {JSON.stringify(resultValue, null, 2)}
                            </pre>
                        </div>
                    ) : (
                        // Show diff view for other entries
                        <div className="flex-1 flex flex-col min-h-0">
                            {/* Diff controls */}
                            <div className="flex items-center justify-between mb-2 shrink-0">
                                <div className="flex bg-gray-200 rounded p-0.5">
                                    <button
                                        onClick={() => setDiffType("event")}
                                        disabled={!hasEventData}
                                        className={`px-2 py-0.5 text-xs font-medium rounded transition-colors ${diffType === "event"
                                            ? "bg-white text-gray-900 shadow-sm"
                                            : "text-gray-500 hover:text-gray-700"
                                            } ${!hasEventData ? "opacity-50 cursor-not-allowed" : ""}`}
                                    >
                                        Event Data
                                    </button>
                                    <button
                                        onClick={() => setDiffType("context")}
                                        disabled={!hasContextData}
                                        className={`px-2 py-0.5 text-xs font-medium rounded transition-colors ${diffType === "context"
                                            ? "bg-white text-gray-900 shadow-sm"
                                            : "text-gray-500 hover:text-gray-700"
                                            } ${!hasContextData ? "opacity-50 cursor-not-allowed" : ""}`}
                                    >
                                        Context Data
                                    </button>
                                </div>
                                <Button
                                    variant="ghost"
                                    size="sm"
                                    onClick={() => setFullscreenOpen(true)}
                                    className="h-7 gap-1"
                                >
                                    <FaExpand className="w-3 h-3" />
                                    <span className="text-xs">Fullscreen</span>
                                </Button>
                            </div>

                            {/* Labels for Before/After */}
                            <div className="flex text-xs text-gray-500 mb-1 shrink-0">
                                <div className="flex-1 flex items-center gap-1">
                                    <FaExchangeAlt className="w-3 h-3" />
                                    <span>Before → After</span>
                                </div>
                            </div>

                            {/* Diff Editor */}
                            <div className="flex-1 min-h-0 border rounded overflow-hidden">
                                <DiffEditor
                                    height="100%"
                                    language="json"
                                    original={beforeContent}
                                    modified={afterContent}
                                    options={{
                                        readOnly: true,
                                        minimap: { enabled: false },
                                        fontSize: 11,
                                        lineNumbers: "off",
                                        scrollBeyondLastLine: false,
                                        automaticLayout: true,
                                        renderSideBySide: true,
                                        enableSplitViewResizing: false,
                                        wordWrap: "on",
                                        renderOverviewRuler: false,
                                        overviewRulerLanes: 0,
                                    }}
                                />
                            </div>
                        </div>
                    )}
                </CardContent>
            </Card>

            {/* Fullscreen Modal */}
            <Dialog open={fullscreenOpen} onOpenChange={setFullscreenOpen}>
                <DialogContent className="max-w-[95vw] w-[95vw] sm:max-w-none max-h-[95vh] h-[95vh] flex flex-col p-0 gap-0">
                    <DialogHeader className="shrink-0 px-6 pt-6 pb-4 border-b">
                        <DialogTitle className="flex items-center justify-between">
                            <span className="text-lg">{entry.alias || entry.type || "Unknown"} - Diff View</span>
                            <div className="flex bg-gray-200 rounded p-0.5 mr-8">
                                <button
                                    onClick={() => setDiffType("event")}
                                    disabled={!hasEventData}
                                    className={`px-4 py-1.5 text-sm font-medium rounded transition-colors ${diffType === "event"
                                        ? "bg-white text-gray-900 shadow-sm"
                                        : "text-gray-500 hover:text-gray-700"
                                        } ${!hasEventData ? "opacity-50 cursor-not-allowed" : ""}`}
                                >
                                    Event Data
                                </button>
                                <button
                                    onClick={() => setDiffType("context")}
                                    disabled={!hasContextData}
                                    className={`px-4 py-1.5 text-sm font-medium rounded transition-colors ${diffType === "context"
                                        ? "bg-white text-gray-900 shadow-sm"
                                        : "text-gray-500 hover:text-gray-700"
                                        } ${!hasContextData ? "opacity-50 cursor-not-allowed" : ""}`}
                                >
                                    Context Data
                                </button>
                            </div>
                        </DialogTitle>
                    </DialogHeader>
                    <div className="flex-1 min-h-0 px-6 pb-6 pt-4">
                        <div className="h-full border rounded overflow-hidden">
                            <DiffEditor
                                height="100%"
                                language="json"
                                original={beforeContent}
                                modified={afterContent}
                                options={{
                                    readOnly: true,
                                    minimap: { enabled: false },
                                    fontSize: 14,
                                    lineNumbers: "on",
                                    scrollBeyondLastLine: false,
                                    automaticLayout: true,
                                    renderSideBySide: true,
                                    wordWrap: "on",
                                    renderOverviewRuler: false,
                                    overviewRulerLanes: 0,
                                }}
                            />
                        </div>
                    </div>
                </DialogContent>
            </Dialog>
        </>
    );
}
