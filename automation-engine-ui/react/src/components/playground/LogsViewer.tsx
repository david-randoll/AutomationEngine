import { useMemo } from "react";
import type { LogEntry } from "@/types/trace";
import { FaCircle } from "react-icons/fa";

interface LogsViewerProps {
    logs?: LogEntry[];
    title?: string;
    className?: string;
    maxHeight?: string;
}

/**
 * Detect log level from formatted message
 */
function detectLogLevel(message: string): "ERROR" | "WARN" | "INFO" | "DEBUG" | "TRACE" {
    const upperMsg = message.toUpperCase();
    if (upperMsg.includes("ERROR") || upperMsg.includes("SEVERE")) return "ERROR";
    if (upperMsg.includes("WARN") || upperMsg.includes("WARNING")) return "WARN";
    if (upperMsg.includes("DEBUG")) return "DEBUG";
    if (upperMsg.includes("TRACE")) return "TRACE";
    return "INFO";
}

/**
 * Get color classes for log level
 */
function getLogLevelColor(level: string): { bg: string; text: string; border: string; icon: string } {
    switch (level) {
        case "ERROR":
            return { bg: "bg-red-50", text: "text-red-700", border: "border-red-200", icon: "text-red-500" };
        case "WARN":
            return { bg: "bg-yellow-50", text: "text-yellow-700", border: "border-yellow-200", icon: "text-yellow-500" };
        case "DEBUG":
            return { bg: "bg-purple-50", text: "text-purple-700", border: "border-purple-200", icon: "text-purple-500" };
        case "TRACE":
            return { bg: "bg-gray-50", text: "text-gray-600", border: "border-gray-200", icon: "text-gray-400" };
        default: // INFO
            return { bg: "bg-blue-50", text: "text-blue-700", border: "border-blue-200", icon: "text-blue-500" };
    }
}

/**
 * Format timestamp to readable string
 */
function formatTimestamp(timestamp?: string): string {
    if (!timestamp) return "";
    try {
        const date = new Date(timestamp);
        return date.toLocaleTimeString("en-US", { 
            hour12: false, 
            hour: "2-digit", 
            minute: "2-digit", 
            second: "2-digit",
            fractionalSecondDigits: 3
        });
    } catch {
        return timestamp;
    }
}

/**
 * Component for displaying logs with color-coded logging levels
 */
export default function LogsViewer({ logs, title = "Logs", className = "", maxHeight = "300px" }: LogsViewerProps) {
    const processedLogs = useMemo(() => {
        if (!logs || logs.length === 0) return [];
        
        return logs.map((log, index) => {
            const message = log.formattedMessage || log.message || "";
            const level = detectLogLevel(message);
            const colors = getLogLevelColor(level);
            const time = formatTimestamp(log.timestamp);
            
            return {
                id: `log-${index}`,
                message,
                level,
                colors,
                time,
                args: log.arguments,
            };
        });
    }, [logs]);

    if (!logs || logs.length === 0) {
        return (
            <div className={`${className} border rounded-lg p-4 bg-gray-50`}>
                {title && <h3 className="text-sm font-medium text-gray-700 mb-2">{title}</h3>}
                <p className="text-xs text-gray-400 italic">No logs captured</p>
            </div>
        );
    }

    return (
        <div className={`${className} border rounded-lg overflow-hidden flex flex-col`}>
            {title && (
                <div className="bg-gray-50 px-3 py-2 border-b shrink-0">
                    <h3 className="text-sm font-medium text-gray-700">{title}</h3>
                </div>
            )}
            <div className="overflow-y-auto font-mono text-xs flex-1" style={{ maxHeight }}>
                {processedLogs.map((log) => (
                    <div
                        key={log.id}
                        className={`px-3 py-2 border-b last:border-b-0 ${log.colors.bg} ${log.colors.border}`}
                    >
                        <div className="flex items-start gap-2">
                            <FaCircle className={`w-2 h-2 mt-1 flex-shrink-0 ${log.colors.icon}`} />
                            <div className="flex-1 min-w-0">
                                <div className="flex items-baseline gap-2 mb-0.5">
                                    <span className={`font-semibold ${log.colors.text}`}>
                                        {log.level}
                                    </span>
                                    {log.time && (
                                        <span className="text-gray-400 text-[10px]">
                                            {log.time}
                                        </span>
                                    )}
                                </div>
                                <div className={`break-words whitespace-pre-wrap ${log.colors.text}`}>
                                    {log.message}
                                </div>
                                {log.args && log.args.length > 0 && (
                                    <details className="mt-1">
                                        <summary className="cursor-pointer text-gray-500 text-[10px] hover:text-gray-700">
                                            Arguments ({log.args.length})
                                        </summary>
                                        <pre className="mt-1 p-2 bg-white rounded border text-[10px] overflow-x-auto">
                                            {JSON.stringify(log.args, null, 2)}
                                        </pre>
                                    </details>
                                )}
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}
