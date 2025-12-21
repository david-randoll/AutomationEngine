import { useMemo } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { TraceEntry, TraceSnapshot } from "@/types/trace";
import { FaCheck, FaTimes, FaClock } from "react-icons/fa";

interface TraceDetailPanelProps {
    entry: TraceEntry | null;
    className?: string;
}

/**
 * Formats a snapshot object as JSON with syntax highlighting.
 */
function SnapshotViewer({ snapshot, label }: { snapshot?: TraceSnapshot; label: string }) {
    if (!snapshot) {
        return (
            <div className="text-sm text-gray-400 italic p-4">
                No {label.toLowerCase()} snapshot available
            </div>
        );
    }

    return (
        <div className="space-y-3">
            {snapshot.eventSnapshot && Object.keys(snapshot.eventSnapshot).length > 0 && (
                <div>
                    <h4 className="text-xs font-semibold text-gray-500 uppercase mb-1">
                        Event Data
                    </h4>
                    <pre className="text-xs bg-gray-50 p-3 rounded-lg overflow-x-auto border">
                        {JSON.stringify(snapshot.eventSnapshot, null, 2)}
                    </pre>
                </div>
            )}
            {snapshot.contextSnapshot && Object.keys(snapshot.contextSnapshot).length > 0 && (
                <div>
                    <h4 className="text-xs font-semibold text-gray-500 uppercase mb-1">
                        Context Data
                    </h4>
                    <pre className="text-xs bg-gray-50 p-3 rounded-lg overflow-x-auto border">
                        {JSON.stringify(snapshot.contextSnapshot, null, 2)}
                    </pre>
                </div>
            )}
            {(!snapshot.eventSnapshot || Object.keys(snapshot.eventSnapshot).length === 0) &&
                (!snapshot.contextSnapshot || Object.keys(snapshot.contextSnapshot).length === 0) && (
                    <div className="text-sm text-gray-400 italic p-4">
                        Empty snapshot
                    </div>
                )}
        </div>
    );
}

/**
 * Panel component for displaying detailed information about a selected trace entry.
 */
export default function TraceDetailPanel({ entry, className }: TraceDetailPanelProps) {
    const duration = useMemo(() => {
        if (!entry?.startedAt || !entry?.finishedAt) return null;
        return entry.finishedAt - entry.startedAt;
    }, [entry?.startedAt, entry?.finishedAt]);

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

    return (
        <Card className={className}>
            <CardHeader className="pb-3">
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
            <CardContent className="pt-0">
                <Tabs defaultValue="before" className="w-full">
                    <TabsList className="mb-3">
                        <TabsTrigger value="before">Before</TabsTrigger>
                        <TabsTrigger value="after">After</TabsTrigger>
                        {entry.category === "result" && (
                            <TabsTrigger value="result">Result</TabsTrigger>
                        )}
                    </TabsList>
                    <ScrollArea className="h-[250px]">
                        <TabsContent value="before" className="mt-0">
                            <SnapshotViewer snapshot={entry.before} label="Before" />
                        </TabsContent>
                        <TabsContent value="after" className="mt-0">
                            <SnapshotViewer snapshot={entry.after} label="After" />
                        </TabsContent>
                        {entry.category === "result" && (
                            <TabsContent value="result" className="mt-0">
                                <div>
                                    <h4 className="text-xs font-semibold text-gray-500 uppercase mb-1">
                                        Result Value
                                    </h4>
                                    <pre className="text-xs bg-gray-50 p-3 rounded-lg overflow-x-auto border">
                                        {JSON.stringify(
                                            (entry as TraceEntry & { result?: unknown }).result,
                                            null,
                                            2
                                        )}
                                    </pre>
                                </div>
                            </TabsContent>
                        )}
                    </ScrollArea>
                </Tabs>
            </CardContent>
        </Card>
    );
}
