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

    // Convert form data to nodes and edges
    const convertToNodesAndEdges = useCallback(() => {
        const data = getValues(pathKey) || {};
        const newNodes: Node[] = [];
        const newEdges: Edge[] = [];

        let yOffset = 0;
        const nodeSpacing = 120;
        let prevNodeId: string | null = null;

        // Add a start node
        const startNode: Node = {
            id: "start",
            type: "workflowNode",
            position: { x: 400, y: yOffset },
            data: {
                label: "Start",
                blockType: "start",
            },
        };
        newNodes.push(startNode);
        prevNodeId = "start";
        yOffset += nodeSpacing;

        // Process each area in order
        for (const area of AREA_ORDER) {
            const pluralArea = `${area}s`; // variables, triggers, etc.
            const items = data[pluralArea] as ModuleType[] | undefined;

            if (items && Array.isArray(items)) {
                for (const [index, item] of items.entries()) {
                    const nodeId = `${area}-${index}`;
                    const areaData = nameToArea(item.name);
                    const blockName = areaData ? Object.values(areaData)[0] : item.name;

                    const node: Node = {
                        id: nodeId,
                        type: "workflowNode",
                        position: { x: 400, y: yOffset },
                        data: {
                            label: (item.alias as string) || blockName || `${area} ${index + 1}`,
                            blockType: area,
                            blockName: item.name,
                            description: item.description as string,
                        },
                    };
                    newNodes.push(node);

                    // Create edge from previous node
                    if (prevNodeId) {
                        newEdges.push({
                            id: `e-${prevNodeId}-${nodeId}`,
                            source: prevNodeId,
                            target: nodeId,
                            type: "smoothstep",
                            animated: true,
                        });
                    }

                    prevNodeId = nodeId;
                    yOffset += nodeSpacing;
                }
            } else if (data[area] && typeof data[area] === "object") {
                // Single block (not array)
                const item = data[area] as ModuleType;
                const nodeId = `${area}-single`;
                const areaData = nameToArea(item.name);
                const blockName = areaData ? Object.values(areaData)[0] : item.name;

                const node: Node = {
                    id: nodeId,
                    type: "workflowNode",
                    position: { x: 400, y: yOffset },
                    data: {
                        label: (item.alias as string) || blockName || area,
                        blockType: area,
                        blockName: item.name,
                        description: item.description as string,
                    },
                };
                newNodes.push(node);

                if (prevNodeId) {
                    newEdges.push({
                        id: `e-${prevNodeId}-${nodeId}`,
                        source: prevNodeId,
                        target: nodeId,
                        type: "smoothstep",
                        animated: true,
                    });
                }

                prevNodeId = nodeId;
                yOffset += nodeSpacing;
            }
        }

        // Add an end node
        const endNode: Node = {
            id: "end",
            type: "workflowNode",
            position: { x: 400, y: yOffset },
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
    }, [getValues, pathKey, setNodes, setEdges]);

    // Initial conversion and watch for changes
    useEffect(() => {
        convertToNodesAndEdges();
    }, [formData, convertToNodesAndEdges]);

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
