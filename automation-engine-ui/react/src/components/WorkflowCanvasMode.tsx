import { useCallback, useEffect, useMemo, useState } from "react";
import { useFormContext } from "react-hook-form";
import {
    ReactFlow,
    Controls,
    Background,
    MiniMap,
    addEdge,
    useNodesState,
    useEdgesState,
    type Node,
    type Edge,
    type Connection,
    type NodeTypes,
    BackgroundVariant,
    Panel,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";

import WorkflowNode, { type WorkflowNodeData } from "./WorkflowNode";
import AddBlockModal from "./AddBlockModal";
import ModuleEditor from "./ModuleEditor";
import { Button } from "./ui/button";
import type { Path, Area, ModuleType } from "@/types/types";
import { nameToArea } from "@/lib/utils";
import { FaPlus, FaTimes, FaExpand, FaCompress } from "react-icons/fa";

interface WorkflowCanvasModeProps {
    path: Path;
}

const nodeTypes: NodeTypes = {
    workflowNode: WorkflowNode as unknown as NodeTypes["workflowNode"],
};

// Areas in execution order
const AREA_ORDER: Area[] = ["variable", "trigger", "condition", "action", "result"];

const WorkflowCanvasMode = ({ path }: WorkflowCanvasModeProps) => {
    const { getValues, setValue, watch } = useFormContext();
    const pathKey = path.join(".");
    const formData = watch(pathKey);

    const [nodes, setNodes, onNodesChange] = useNodesState<Node>([]);
    const [edges, setEdges, onEdgesChange] = useEdgesState<Edge>([]);

    const [modalOpen, setModalOpen] = useState(false);
    const [modalType, setModalType] = useState<Area>("action");

    const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null);
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const [isFullscreen, setIsFullscreen] = useState(false);
    const [refreshTrigger, setRefreshTrigger] = useState(0);

    // Convert form data to nodes and edges
    useEffect(() => {
        const data = formData || {};
        const newNodes: Node[] = [];
        const newEdges: Edge[] = [];

        let nodeIdCounter = 0;
        const nodeSpacing = 150; // Reduced from 300 - shorter parent lines
        const verticalSpacing = 80; // Reduced from 100 - tighter child spacing
        let prevNodeId: string | null = null;

        // Helper to get display label: alias > type/name > "Unnamed"
        const getBlockLabel = (item: ModuleType, area?: Area): string => {
            if (item.alias && typeof item.alias === "string" && item.alias.trim()) {
                return item.alias;
            }
            // Try to get the type field (e.g., trigger, condition, action, variable, result)
            if (area && item[area] && typeof item[area] === "string") {
                return item[area] as string;
            }
            const typeField = item.trigger || item.condition || item.action || item.variable || item.result || item.name;
            if (typeField && typeof typeField === "string" && typeField.trim()) {
                return typeField;
            }
            return "Unnamed";
        };

        // Detect the area type from x-block-type or field names
        const detectAreaType = (item: any): Area => {
            // Check for x-block-type first
            if (item["x-block-type"]) {
                return item["x-block-type"] as Area;
            }
            // Fallback to field detection
            if (item.action) return "action";
            if (item.trigger) return "trigger";
            if (item.condition) return "condition";
            if (item.variable) return "variable";
            if (item.result) return "result";
            return "action"; // default fallback
        };

        // Check if an object has x-block-type (is a block)
        const isBlock = (value: any): boolean => {
            return value && typeof value === "object" && (value["x-block-type"] || value.action || value.trigger || value.condition || value.variable || value.result);
        };

        // Top-to-bottom layout: Y increases downward, X increases for children (depth)
        const horizontalSpacing = 350; // Increased from 250 - longer child lines

        // Add a start node at top center
        const startNode: Node = {
            id: "start",
            type: "workflowNode",
            position: { x: 200, y: 0 },
            data: {
                label: "Start",
                blockType: "start",
            },
        };
        newNodes.push(startNode);
        prevNodeId = "start";

        let currentY = nodeSpacing;
        let currentX = 200;

        /**
         * Process a block and its children recursively (top-to-bottom layout)
         */
        const processBlock = (
            item: ModuleType,
            area: Area,
            index: number,
            parentId: string | null,
            depth: number = 0
        ): { nodeId: string; maxX: number; maxY: number } => {
            const nodeId = `${area}-${index}-${nodeIdCounter++}`;

            // Position: X based on depth (children branch right), Y flows down
            const x = currentX + depth * horizontalSpacing;
            const y = currentY;

            const node: Node = {
                id: nodeId,
                type: "workflowNode",
                position: { x, y },
                data: {
                    label: getBlockLabel(item, area),
                    blockType: area,
                    blockName: item[area] || item.name,
                    description: item.description as string,
                },
            };
            newNodes.push(node);

            // Create edge from parent
            if (parentId) {
                newEdges.push({
                    id: `e-${parentId}-${nodeId}`,
                    source: parentId,
                    target: nodeId,
                    type: "smoothstep",
                    animated: true,
                });
            }

            let maxX = x;
            let maxY = y;

            // Dynamically process all fields in the item that contain blocks
            for (const [fieldName, fieldValue] of Object.entries(item)) {
                // Skip non-block fields
                if (fieldName === "alias" || fieldName === "description" || fieldName === "name" || fieldName === "x-block-type") {
                    continue;
                }

                if (Array.isArray(fieldValue)) {
                    // Process array of blocks
                    for (const [childIndex, childItem] of fieldValue.entries()) {
                        if (isBlock(childItem)) {
                            currentY += verticalSpacing;
                            const childArea = detectAreaType(childItem);
                            const childResult = processBlock(childItem, childArea, childIndex, nodeId, depth + 1);
                            maxX = Math.max(maxX, childResult.maxX);
                            maxY = Math.max(maxY, childResult.maxY);
                        }
                    }
                } else if (isBlock(fieldValue)) {
                    // Process single block object
                    currentY += verticalSpacing;
                    const childArea = detectAreaType(fieldValue);
                    const childResult = processBlock(fieldValue as ModuleType, childArea, 0, nodeId, depth + 1);
                    maxX = Math.max(maxX, childResult.maxX);
                    maxY = Math.max(maxY, childResult.maxY);
                }
            }

            return { nodeId, maxX, maxY: currentY };
        };

        // Process each area in order (top to bottom)
        for (const area of AREA_ORDER) {
            const pluralArea = `${area}s`;
            const items = data[pluralArea] as ModuleType[] | undefined;

            if (items && Array.isArray(items)) {
                for (const [index, item] of items.entries()) {
                    const result = processBlock(item, area, index, prevNodeId, 0);
                    prevNodeId = result.nodeId;
                    currentY = result.maxY + nodeSpacing;
                    currentX = 200; // Reset X for next top-level block
                }
            } else if (data[area] && typeof data[area] === "object") {
                // Single block (not array)
                const item = data[area] as ModuleType;
                const result = processBlock(item, area, 0, prevNodeId, 0);
                prevNodeId = result.nodeId;
                currentY = result.maxY + nodeSpacing;
                currentX = 200;
            }
        }

        // Add an end node at the bottom
        const endNode: Node = {
            id: "end",
            type: "workflowNode",
            position: { x: 200, y: currentY },
            data: {
                label: "End",
                blockType: "end",
            },
        };
        newNodes.push(endNode);

        if (prevNodeId && prevNodeId !== "start") {
            newEdges.push({
                id: `e-${prevNodeId}-end`,
                source: prevNodeId,
                target: "end",
                type: "smoothstep",
                animated: true,
            });
        } else {
            // Connect start to end if no blocks
            newEdges.push({
                id: "e-start-end",
                source: "start",
                target: "end",
                type: "smoothstep",
                animated: true,
            });
        }

        setNodes(newNodes);
        setEdges(newEdges);
    }, [formData, setNodes, setEdges, refreshTrigger]);

    const onConnect = useCallback(
        (params: Connection) => setEdges((eds) => addEdge({ ...params, type: "smoothstep", animated: true }, eds)),
        [setEdges]
    );

    const onNodeClick = useCallback((_: React.MouseEvent, node: Node) => {
        const nodeData = node.data as WorkflowNodeData;
        if (nodeData.blockType === "start" || nodeData.blockType === "end") {
            return;
        }
        setSelectedNodeId(node.id);
        setSidebarOpen(true);
    }, []);

    // Get the path for the selected node
    const selectedNodePath = useMemo((): Path | null => {
        if (!selectedNodeId) return null;

        const [area, indexStr] = selectedNodeId.split("-");
        if (indexStr === "single") {
            return [...path, area];
        }
        const index = Number.parseInt(indexStr, 10);
        if (Number.isNaN(index)) return null;

        return [...path, `${area}s`, index];
    }, [selectedNodeId, path]);

    // Get the selected node's data for the editor
    const selectedNodeData = useMemo((): ModuleType | null => {
        if (!selectedNodePath) return null;
        return getValues(selectedNodePath.join("."));
    }, [selectedNodePath, getValues]);

    const handleAddBlock = (type: Area) => {
        setModalType(type);
        setModalOpen(true);
    };

    const handleModalSelect = (modFromServer: ModuleType) => {
        const { name, label } = modFromServer;
        const instance: ModuleType = {
            ...nameToArea(name),
            alias: label,
        };

        const pluralArea = `${modalType}s`;
        const current: ModuleType[] = getValues(`${pathKey}.${pluralArea}`) || [];
        setValue(`${pathKey}.${pluralArea}`, [...current, instance], {
            shouldValidate: true,
            shouldDirty: true,
            shouldTouch: true,
        });

        setModalOpen(false);
        setRefreshTrigger(prev => prev + 1);
    };

    const handleDeleteNode = useCallback(() => {
        if (!selectedNodeId) return;

        const [area, indexStr] = selectedNodeId.split("-");
        const pluralArea = `${area}s`;

        if (indexStr === "single") {
            setValue(`${pathKey}.${area}`, undefined, {
                shouldValidate: true,
                shouldDirty: true,
            });
        } else {
            const index = Number.parseInt(indexStr, 10);
            const items: ModuleType[] = getValues(`${pathKey}.${pluralArea}`) || [];
            const newItems = items.filter((_, i) => i !== index);
            setValue(`${pathKey}.${pluralArea}`, newItems, {
                shouldValidate: true,
                shouldDirty: true,
            });
        }

        setSidebarOpen(false);
        setSelectedNodeId(null);
        setRefreshTrigger(prev => prev + 1);
    }, [selectedNodeId, pathKey, setValue, getValues]);

    return (
        <div className={`flex ${isFullscreen ? "fixed inset-0 z-50" : "h-full"} bg-white rounded-lg border border-gray-200 shadow-sm overflow-hidden`}>
            {/* Main Canvas */}
            <div className="flex-1 relative">
                <ReactFlow
                    nodes={nodes}
                    edges={edges}
                    onNodesChange={onNodesChange}
                    onEdgesChange={onEdgesChange}
                    onConnect={onConnect}
                    onNodeClick={onNodeClick}
                    nodeTypes={nodeTypes}
                    fitView
                    attributionPosition="bottom-left"
                    proOptions={{ hideAttribution: true }}
                >
                    <Background variant={BackgroundVariant.Dots} gap={20} size={1} color="#e5e7eb" />
                    <Controls className="!bg-white !border !border-gray-200 !shadow-sm" />
                    <MiniMap
                        nodeColor={(node) => {
                            const type = (node.data as unknown as WorkflowNodeData)?.blockType;
                            switch (type) {
                                case "trigger": return "#a855f7";
                                case "variable": return "#3b82f6";
                                case "condition": return "#eab308";
                                case "action": return "#22c55e";
                                case "result": return "#ef4444";
                                default: return "#9ca3af";
                            }
                        }}
                        className="!bg-gray-50 !border !border-gray-200"
                    />

                    {/* Add Block Panel */}
                    <Panel position="top-left" className="!m-2">
                        <div className="flex flex-col gap-2 bg-white p-2 rounded-lg border border-gray-200 shadow-sm">
                            <span className="text-xs font-medium text-gray-500 px-1">Add Block</span>
                            {AREA_ORDER.map((area) => (
                                <Button
                                    key={area}
                                    variant="outline"
                                    size="sm"
                                    onClick={() => handleAddBlock(area)}
                                    className="justify-start text-xs capitalize"
                                >
                                    <FaPlus className="w-3 h-3 mr-2" />
                                    {area}
                                </Button>
                            ))}
                        </div>
                    </Panel>

                    {/* Fullscreen Toggle */}
                    <Panel position="top-right" className="!m-2">
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => setIsFullscreen(!isFullscreen)}
                            className="bg-white"
                        >
                            {isFullscreen ? <FaCompress className="w-4 h-4" /> : <FaExpand className="w-4 h-4" />}
                        </Button>
                    </Panel>
                </ReactFlow>
            </div>

            {/* Side Panel for Node Editing */}
            {sidebarOpen && selectedNodePath && selectedNodeData && (
                <div className="w-[400px] border-l border-gray-200 bg-white flex flex-col overflow-hidden">
                    <div className="flex items-center justify-between px-4 py-3 border-b border-gray-200 bg-gray-50">
                        <h3 className="text-sm font-semibold text-gray-700">Edit Block</h3>
                        <div className="flex gap-2">
                            <Button
                                variant="ghost"
                                size="sm"
                                onClick={handleDeleteNode}
                                className="text-red-500 hover:text-red-700 hover:bg-red-50"
                            >
                                Delete
                            </Button>
                            <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => {
                                    setSidebarOpen(false);
                                    setSelectedNodeId(null);
                                }}
                            >
                                <FaTimes className="w-4 h-4" />
                            </Button>
                        </div>
                    </div>
                    <div className="flex-1 overflow-auto p-4">
                        <ModuleEditor
                            key={selectedNodePath.join(".")}
                            module={selectedNodeData}
                            path={selectedNodePath}
                        />
                    </div>
                </div>
            )}

            {/* Add Block Modal */}
            <AddBlockModal
                open={modalOpen}
                onOpenChange={setModalOpen}
                type={modalType}
                onSelect={handleModalSelect}
            />
        </div>
    );
};

export default WorkflowCanvasMode;
