import { memo } from "react";
import { Handle, Position, type NodeProps } from "@xyflow/react";
import type { Area } from "@/types/types";
import {
    FaBolt,
    FaPlay,
    FaCodeBranch,
    FaCog,
    FaDatabase,
    FaFlag,
} from "react-icons/fa";

export interface WorkflowNodeData extends Record<string, unknown> {
    label: string;
    blockType: Area | "start" | "end";
    blockName?: string;
    description?: string;
    isSelected?: boolean;
    onEdit?: () => void;
    onDelete?: () => void;
}

const areaColors: Record<string, { bg: string; border: string; icon: string }> = {
    trigger: { bg: "bg-purple-50", border: "border-purple-400", icon: "text-purple-600" },
    variable: { bg: "bg-blue-50", border: "border-blue-400", icon: "text-blue-600" },
    condition: { bg: "bg-yellow-50", border: "border-yellow-400", icon: "text-yellow-600" },
    action: { bg: "bg-green-50", border: "border-green-400", icon: "text-green-600" },
    result: { bg: "bg-red-50", border: "border-red-400", icon: "text-red-600" },
    start: { bg: "bg-gray-100", border: "border-gray-400", icon: "text-gray-600" },
    end: { bg: "bg-gray-100", border: "border-gray-400", icon: "text-gray-600" },
};

const areaIcons: Record<string, React.ReactNode> = {
    trigger: <FaBolt className="w-4 h-4" />,
    variable: <FaDatabase className="w-4 h-4" />,
    condition: <FaCodeBranch className="w-4 h-4" />,
    action: <FaCog className="w-4 h-4" />,
    result: <FaFlag className="w-4 h-4" />,
    start: <FaPlay className="w-4 h-4" />,
    end: <FaFlag className="w-4 h-4" />,
};

const WorkflowNode = memo(({ data, selected }: NodeProps & { data: WorkflowNodeData }) => {
    const blockType = data.blockType || "action";
    const colors = areaColors[blockType] || areaColors.action;
    const icon = areaIcons[blockType] || areaIcons.action;

    const isTerminal = blockType === "start" || blockType === "end";

    return (
        <div
            className={`
                relative min-w-[180px] max-w-[280px] rounded-lg border-2 shadow-md transition-all duration-200
                ${colors.bg} ${colors.border}
                ${selected ? "ring-2 ring-blue-500 ring-offset-2" : ""}
                ${isTerminal ? "" : "cursor-pointer hover:shadow-lg"}
            `}
        >
            {/* Input Handle */}
            {blockType !== "start" && (
                <Handle
                    type="target"
                    position={Position.Left}
                    className="!w-3 !h-3 !bg-gray-400 !border-2 !border-white"
                />
            )}

            {/* Header */}
            <div className={`flex items-center gap-2 px-3 py-2 border-b ${colors.border} bg-white/50`}>
                <span className={colors.icon}>{icon}</span>
                <span className="text-xs font-semibold uppercase text-gray-500">{blockType}</span>
            </div>

            {/* Body */}
            <div className="px-3 py-2">
                <div className="font-medium text-gray-900 text-sm truncate">{data.label || "Unnamed"}</div>
                {data.blockName && (
                    <div className="text-xs text-gray-500 truncate mt-0.5">{data.blockName}</div>
                )}
                {data.description && (
                    <div className="text-xs text-gray-400 truncate mt-1">{data.description}</div>
                )}
            </div>

            {/* Action Buttons (shown on hover/select) */}
            {!isTerminal && (data.onEdit || data.onDelete) && (
                <div
                    className={`
                        absolute -top-2 -right-2 flex gap-1 opacity-0 transition-opacity duration-200
                        ${selected ? "opacity-100" : "group-hover:opacity-100"}
                    `}
                >
                    {data.onEdit && (
                        <button
                            onClick={(e) => {
                                e.stopPropagation();
                                data.onEdit?.();
                            }}
                            className="w-6 h-6 rounded-full bg-blue-500 text-white text-xs flex items-center justify-center shadow hover:bg-blue-600"
                        >
                            ✎
                        </button>
                    )}
                    {data.onDelete && (
                        <button
                            onClick={(e) => {
                                e.stopPropagation();
                                data.onDelete?.();
                            }}
                            className="w-6 h-6 rounded-full bg-red-500 text-white text-xs flex items-center justify-center shadow hover:bg-red-600"
                        >
                            ×
                        </button>
                    )}
                </div>
            )}

            {/* Output Handle */}
            {blockType !== "end" && (
                <Handle
                    type="source"
                    position={Position.Right}
                    className="!w-3 !h-3 !bg-gray-400 !border-2 !border-white"
                />
            )}
        </div>
    );
});

WorkflowNode.displayName = "WorkflowNode";

export default WorkflowNode;
