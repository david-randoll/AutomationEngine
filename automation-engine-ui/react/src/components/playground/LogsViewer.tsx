import { useMemo } from "react";
import type { LogEntry } from "@/types/trace";

interface LogsViewerProps {
    logs?: LogEntry[];
    title?: string;
    className?: string;
    maxHeight?: string;
}

/**
 * Get ANSI-style color for log level in terminal display
 */
function getLogLevelColor(level?: string): string {
    switch (level?.toUpperCase()) {
        case "ERROR":
            return "text-red-500";
        case "WARN":
            return "text-yellow-500";
        case "DEBUG":
            return "text-purple-500";
        case "TRACE":
            return "text-gray-400";
        default: // INFO
            return "text-green-500";
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
            const level = log.level || "INFO";
            const color = getLogLevelColor(level);
            const time = formatTimestamp(log.timestamp);
            
            return {
                id: `log-${index}`,
                message,
                level,
                color,
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
        <div className={`${className} rounded-lg overflow-hidden flex flex-col bg-black`}>
            {title && (
                <div className="bg-gray-900 px-3 py-2 border-b border-gray-800 shrink-0">
                    <h3 className="text-sm font-medium text-gray-300">{title}</h3>
                </div>
            )}
            <div className="overflow-y-auto font-mono text-xs flex-1 p-3 text-gray-300" style={{ maxHeight }}>
                {processedLogs.map((log) => (
                    <div key={log.id} className="mb-1 leading-relaxed">
                        <span className="text-gray-500">[{log.time || "--:--:--"}]</span>
                        {" "}
                        <span className={`font-bold ${log.color}`}>[{log.level}]</span>
                        {" "}
                        <span className="text-gray-200">{log.message}</span>
                        {log.args && log.args.length > 0 && (
                            <div className="ml-16 mt-0.5 text-gray-400 text-[10px]">
                                args: {JSON.stringify(log.args)}
                            </div>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
}
