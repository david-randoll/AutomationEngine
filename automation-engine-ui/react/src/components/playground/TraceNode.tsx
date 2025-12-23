import { memo } from "react";
import { Handle, Position, type NodeProps } from "@xyflow/react";
import type { TraceEntryCategory } from "@/types/trace";
import {
    FaBolt,
    FaCodeBranch,
    FaCog,
    FaDatabase,
    FaFlag,
    FaCheck,
    FaTimes,
} from "react-icons/fa";

export interface TraceNodeData extends Record<string, unknown> {
    label: string;
    category: TraceEntryCategory;
    type?: string;
    alias?: string;
    duration?: number;
    activated?: boolean; // for triggers
    satisfied?: boolean; // for conditions
    hasChildren?: boolean;
    isSelected?: boolean;
}

const categoryColors: Record<
    TraceEntryCategory,
    { bg: string; border: string; icon: string; bgSuccess: string; bgFail: string }
> = {
    variable: {
        bg: "bg-purple-50",
        border: "border-purple-400",
        icon: "text-purple-600",
        bgSuccess: "bg-purple-100",
        bgFail: "bg-purple-50",
    },
    trigger: {
        bg: "bg-blue-50",
        border: "border-blue-400",
        icon: "text-blue-600",
        bgSuccess: "bg-blue-100",
        bgFail: "bg-blue-50",
    },
    condition: {
        bg: "bg-yellow-50",
        border: "border-yellow-400",
        icon: "text-yellow-600",
        bgSuccess: "bg-green-50",
        bgFail: "bg-red-50",
    },
    action: {
        bg: "bg-green-50",
        border: "border-green-400",
        icon: "text-green-600",
        bgSuccess: "bg-green-100",
        bgFail: "bg-green-50",
    },
    result: {
        bg: "bg-red-50",
        border: "border-red-400",
        icon: "text-red-600",
        bgSuccess: "bg-red-100",
        bgFail: "bg-red-50",
    },
};

const categoryIcons: Record<TraceEntryCategory, React.ReactNode> = {
    trigger: <FaBolt className="w-3.5 h-3.5" />,
    variable: <FaDatabase className="w-3.5 h-3.5" />,
    condition: <FaCodeBranch className="w-3.5 h-3.5" />,
    action: <FaCog className="w-3.5 h-3.5" />,
    result: <FaFlag className="w-3.5 h-3.5" />,
};

/**
 * Custom node component for displaying trace entries in React Flow.
 */
const TraceNode = memo(
    ({ data, selected }: NodeProps & { data: TraceNodeData }) => {
        const category = data.category || "action";
        const colors = categoryColors[category] || categoryColors.action;
        const icon = categoryIcons[category] || categoryIcons.action;

        // Determine background based on activation/satisfaction state
        let bgColor = colors.bg;
        if (category === "trigger" && data.activated !== undefined) {
            bgColor = data.activated ? colors.bgSuccess : colors.bgFail;
        } else if (category === "condition" && data.satisfied !== undefined) {
            bgColor = data.satisfied ? colors.bgSuccess : colors.bgFail;
        }

        // Format duration
        const durationText = data.duration
            ? data.duration < 1
                ? "<1ms"
                : `${data.duration}ms`
            : undefined;

        return (
            <div
                className={`
          relative min-w-[160px] max-w-[220px] rounded-lg border-2 shadow-md transition-all duration-200
          ${bgColor} ${colors.border}
          ${selected ? "ring-2 ring-blue-500 ring-offset-2" : ""}
          cursor-pointer hover:shadow-lg
        `}
            >
                {/* Input Handle */}
                <Handle
                    type="target"
                    position={Position.Top}
                    className="!w-2.5 !h-2.5 !bg-gray-400 !border-2 !border-white"
                />

                {/* Header */}
                <div
                    className={`flex items-center justify-between gap-2 px-2.5 py-1.5 border-b ${colors.border} bg-white/60`}
                >
                    <div className="flex items-center gap-1.5">
                        <span className={colors.icon}>{icon}</span>
                        <span className="text-xs font-semibold uppercase text-gray-500">
                            {category}
                        </span>
                    </div>
                    {/* Status indicator */}
                    {category === "trigger" && data.activated !== undefined && (
                        <span
                            className={`flex items-center gap-0.5 text-xs ${data.activated ? "text-green-600" : "text-gray-400"
                                }`}
                        >
                            {data.activated ? (
                                <FaCheck className="w-3 h-3" />
                            ) : (
                                <FaTimes className="w-3 h-3" />
                            )}
                        </span>
                    )}
                    {category === "condition" && data.satisfied !== undefined && (
                        <span
                            className={`flex items-center gap-0.5 text-xs ${data.satisfied ? "text-green-600" : "text-red-500"
                                }`}
                        >
                            {data.satisfied ? (
                                <FaCheck className="w-3 h-3" />
                            ) : (
                                <FaTimes className="w-3 h-3" />
                            )}
                        </span>
                    )}
                </div>

                {/* Body */}
                <div className="px-2.5 py-2">
                    <div className="font-medium text-gray-900 text-sm truncate">
                        {data.alias || data.type || "Unknown"}
                    </div>
                    {data.type && data.alias && (
                        <div className="text-xs text-gray-500 truncate mt-0.5">
                            {data.type}
                        </div>
                    )}
                    {durationText && (
                        <div className="text-xs text-gray-400 mt-1 flex items-center gap-1">
                            <span className="opacity-60">⏱</span>
                            {durationText}
                        </div>
                    )}
                </div>

                {/* Children indicator */}
                {data.hasChildren && (
                    <div className="absolute -bottom-1 left-1/2 -translate-x-1/2 w-4 h-4 rounded-full bg-gray-200 border border-gray-300 flex items-center justify-center">
                        <span className="text-[10px] text-gray-500">+</span>
                    </div>
                )}

                {/* Output Handle */}
                <Handle
                    type="source"
                    position={Position.Bottom}
                    className="!w-2.5 !h-2.5 !bg-gray-400 !border-2 !border-white"
                />
            </div>
        );
    }
);

TraceNode.displayName = "TraceNode";

export default TraceNode;
