import { useCallback, useMemo, useState } from "react";
import {
    ReactFlow,
    Controls,
    Background,
    useNodesState,
    useEdgesState,
    type Node,
    type Edge,
    type NodeTypes,
    BackgroundVariant,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { FaGripVertical } from "react-icons/fa";

import TraceNode, { type TraceNodeData } from "./TraceNode";
import TraceDetailPanel from "./TraceDetailPanel";
import type {
    ExecutionTrace,
    TraceEntry,
    TraceEntryCategory,
    TraceData,
    TraceChildren,
    VariableTraceEntry,
    TriggerTraceEntry,
    ConditionTraceEntry,
    ActionTraceEntry,
    ResultTraceEntry,
} from "@/types/trace";

interface TraceCanvasProps {
    trace: ExecutionTrace | null;
    className?: string;
    rightPanelWidth?: number;
    onRightPanelWidthChange?: (width: number) => void;
    onRightResizeStart?: () => void;
}

const nodeTypes: NodeTypes = {
    traceNode: TraceNode as unknown as NodeTypes["traceNode"],
};

// Node sizing constants
const NODE_WIDTH = 180;
const NODE_HEIGHT = 80;
const HORIZONTAL_GAP = 40;
const VERTICAL_GAP = 60;

/**
 * Converts trace data into React Flow nodes and edges with hierarchical layout.
 */
function traceToNodesAndEdges(trace: ExecutionTrace | null): {
    nodes: Node<TraceNodeData>[];
    edges: Edge[];
    entries: Map<string, TraceEntry>;
} {
    const nodes: Node<TraceNodeData>[] = [];
    const edges: Edge[] = [];
    const entries = new Map<string, TraceEntry>();

    if (!trace?.trace) {
        return { nodes, edges, entries };
    }

    let nodeId = 0;
    let currentY = 0;
    let prevNodeId: string | null = null;

    /**
     * Process a list of entries and create nodes for them.
     */
    function processEntries<T extends VariableTraceEntry | TriggerTraceEntry | ConditionTraceEntry | ActionTraceEntry | ResultTraceEntry>(
        entryList: T[] | undefined,
        category: TraceEntryCategory,
        parentId: string | null = null,
        startX: number = 0,
        depth: number = 0
    ): { lastNodeId: string | null; maxX: number; maxY: number } {
        if (!entryList || entryList.length === 0) {
            return { lastNodeId: parentId, maxX: startX, maxY: currentY };
        }

        let lastCreatedNodeId: string | null = null;
        let maxX = startX;

        for (const entry of entryList) {
            const id = `node-${nodeId++}`;
            const duration =
                entry.startedAt && entry.finishedAt
                    ? entry.finishedAt - entry.startedAt
                    : undefined;

            const traceEntry: TraceEntry = {
                ...entry,
                category,
                id,
            } as TraceEntry;
            entries.set(id, traceEntry);

            const node: Node<TraceNodeData> = {
                id,
                type: "traceNode",
                position: { x: startX + depth * (NODE_WIDTH + HORIZONTAL_GAP), y: currentY },
                data: {
                    label: entry.alias || entry.type || "Unknown",
                    category,
                    type: entry.type,
                    alias: entry.alias,
                    duration,
                    activated: (entry as TriggerTraceEntry).activated,
                    satisfied: (entry as ConditionTraceEntry).satisfied,
                    hasChildren: !!entry.children,
                },
            };
            nodes.push(node);
            maxX = Math.max(maxX, node.position.x + NODE_WIDTH);

            // Connect from parent or previous node
            if (parentId && lastCreatedNodeId === null) {
                edges.push({
                    id: `edge-${parentId}-${id}`,
                    source: parentId,
                    target: id,
                    type: "smoothstep",
                    animated: false,
                });
            } else if (prevNodeId && depth === 0) {
                edges.push({
                    id: `edge-${prevNodeId}-${id}`,
                    source: prevNodeId,
                    target: id,
                    type: "smoothstep",
                    animated: false,
                });
            } else if (lastCreatedNodeId) {
                edges.push({
                    id: `edge-${lastCreatedNodeId}-${id}`,
                    source: lastCreatedNodeId,
                    target: id,
                    type: "smoothstep",
                    animated: false,
                });
            }

            lastCreatedNodeId = id;
            currentY += NODE_HEIGHT + VERTICAL_GAP;

            // Process children recursively
            if (entry.children) {
                const childrenResult = processChildren(entry.children, id, startX, depth + 1);
                maxX = Math.max(maxX, childrenResult.maxX);
            }

            if (depth === 0) {
                prevNodeId = id;
            }
        }

        return { lastNodeId: lastCreatedNodeId, maxX, maxY: currentY };
    }

    /**
     * Process nested children (for composite actions like ifThenElse).
     */
    function processChildren(
        children: TraceChildren,
        parentId: string,
        startX: number,
        depth: number
    ): { maxX: number; maxY: number } {
        let maxX = startX;

        // Process in order: variables, triggers, conditions, actions, result
        const result1 = processEntries(children.variables, "variable", parentId, startX, depth);
        maxX = Math.max(maxX, result1.maxX);

        const result2 = processEntries(children.triggers, "trigger", result1.lastNodeId, startX, depth);
        maxX = Math.max(maxX, result2.maxX);

        const result3 = processEntries(children.conditions, "condition", result2.lastNodeId, startX, depth);
        maxX = Math.max(maxX, result3.maxX);

        const result4 = processEntries(children.actions, "action", result3.lastNodeId, startX, depth);
        maxX = Math.max(maxX, result4.maxX);

        if (children.result) {
            const result5 = processEntries([children.result], "result", result4.lastNodeId, startX, depth);
            maxX = Math.max(maxX, result5.maxX);
        }

        return { maxX, maxY: currentY };
    }

    // Process main trace data
    const traceData: TraceData = trace.trace;

    processEntries(traceData.variables, "variable");
    processEntries(traceData.triggers, "trigger");
    processEntries(traceData.conditions, "condition");
    processEntries(traceData.actions, "action");

    if (traceData.result) {
        processEntries([traceData.result], "result");
    }

    return { nodes, edges, entries };
}

/**
 * React Flow canvas component for visualizing execution traces.
 */
export default function TraceCanvas({
    trace,
    className,
    rightPanelWidth = 350,
    onRightResizeStart,
}: TraceCanvasProps) {
    const [selectedEntry, setSelectedEntry] = useState<TraceEntry | null>(null);

    // Convert trace to nodes and edges
    const { nodes: initialNodes, edges: initialEdges, entries } = useMemo(
        () => traceToNodesAndEdges(trace),
        [trace]
    );

    const [nodes, , onNodesChange] = useNodesState(initialNodes);
    const [edges, , onEdgesChange] = useEdgesState(initialEdges);

    // Handle node selection
    const onNodeClick = useCallback(
        (_: React.MouseEvent, node: Node) => {
            const entry = entries.get(node.id);
            setSelectedEntry(entry || null);
        },
        [entries]
    );

    const onPaneClick = useCallback(() => {
        setSelectedEntry(null);
    }, []);

    // Calculate summary stats
    const stats = useMemo(() => {
        if (!trace) return null;
        const duration =
            trace.startedAt && trace.finishedAt
                ? trace.finishedAt - trace.startedAt
                : null;
        return {
            alias: trace.alias || "Unnamed",
            executionId: trace.executionId,
            duration: duration !== null ? (duration < 1 ? "<1ms" : `${duration}ms`) : "N/A",
            nodeCount: nodes.length,
        };
    }, [trace, nodes.length]);

    if (!trace) {
        return (
            <div className={`flex items-center justify-center h-full bg-gray-50 ${className}`}>
                <p className="text-gray-400">No trace available</p>
            </div>
        );
    }

    return (
        <div className={`flex h-full ${className}`}>
            {/* React Flow Canvas */}
            <div className="flex-1 relative">
                <ReactFlow
                    nodes={nodes}
                    edges={edges}
                    onNodesChange={onNodesChange}
                    onEdgesChange={onEdgesChange}
                    onNodeClick={onNodeClick}
                    onPaneClick={onPaneClick}
                    nodeTypes={nodeTypes}
                    fitView
                    fitViewOptions={{ padding: 0.2 }}
                    minZoom={0.1}
                    maxZoom={2}
                    proOptions={{ hideAttribution: true }}
                >
                    <Background variant={BackgroundVariant.Dots} gap={16} size={1} />
                    <Controls />

                    {/* Stats overlay */}
                    {stats && (
                        <div className="absolute top-4 left-4 bg-white/90 backdrop-blur-sm rounded-lg shadow-sm border px-4 py-3 z-10">
                            <h3 className="font-semibold text-sm text-gray-900">{stats.alias}</h3>
                            <div className="flex items-center gap-4 text-xs text-gray-500 mt-1">
                                <span>Duration: {stats.duration}</span>
                                <span>Steps: {stats.nodeCount}</span>
                            </div>
                            {stats.executionId && (
                                <div className="text-xs text-gray-400 mt-1 font-mono truncate max-w-[200px]">
                                    ID: {stats.executionId}
                                </div>
                            )}
                        </div>
                    )}
                </ReactFlow>
            </div>

            {/* Detail Panel */}
            <div
                style={{ width: `${rightPanelWidth}px` }}
                className="border-l bg-white flex flex-col overflow-hidden shrink-0 relative"
            >
                {/* Right resize handle */}
                <div
                    onMouseDown={onRightResizeStart}
                    className="absolute top-0 left-0 w-1 h-full cursor-col-resize hover:bg-blue-400 transition-colors group z-10"
                >
                    <div className="absolute top-1/2 left-0 -translate-y-1/2 -translate-x-1/2 opacity-0 group-hover:opacity-100 transition-opacity">
                        <FaGripVertical className="w-3 h-3 text-gray-400" />
                    </div>
                </div>
                <TraceDetailPanel entry={selectedEntry} className="flex-1 min-h-0 border-0 rounded-none shadow-none" />
            </div>
        </div>
    );
}
