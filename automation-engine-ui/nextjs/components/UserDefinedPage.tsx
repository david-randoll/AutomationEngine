"use client";

import { useState, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import { userDefinedApi } from "@/lib/user-defined-api";
import type { BlockType, UserDefinedDefinition } from "@/types/user-defined";
import { toast } from "sonner";
import { PlusIcon, TrashIcon, RefreshCwIcon, CodeIcon, ZapIcon, FilterIcon, VariableIcon } from "lucide-react";

interface BlockTypeConfig {
    type: BlockType;
    label: string;
    singularLabel: string;
    description: string;
    icon: React.ReactNode;
}

const blockTypeConfigs: BlockTypeConfig[] = [
    {
        type: "actions",
        label: "Actions",
        singularLabel: "Action",
        description: "User-defined reusable actions",
        icon: <ZapIcon className="size-4" />,
    },
    {
        type: "conditions",
        label: "Conditions",
        singularLabel: "Condition",
        description: "User-defined reusable conditions",
        icon: <FilterIcon className="size-4" />,
    },
    {
        type: "triggers",
        label: "Triggers",
        singularLabel: "Trigger",
        description: "User-defined reusable triggers",
        icon: <CodeIcon className="size-4" />,
    },
    {
        type: "variables",
        label: "Variables",
        singularLabel: "Variable",
        description: "User-defined reusable variables",
        icon: <VariableIcon className="size-4" />,
    },
];

interface DefinitionListProps {
    type: BlockType;
    definitions: Record<string, UserDefinedDefinition>;
    onRefresh: () => void;
    onUnregister: (name: string) => void;
    isLoading: boolean;
}

const DefinitionList = ({ type, definitions, onRefresh, onUnregister, isLoading }: DefinitionListProps) => {
    const entries = Object.entries(definitions);

    return (
        <div className="space-y-3">
            <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">
                    {entries.length} registered {type}
                </span>
                <Button variant="ghost" size="sm" onClick={onRefresh} disabled={isLoading}>
                    <RefreshCwIcon className={`size-4 ${isLoading ? "animate-spin" : ""}`} />
                </Button>
            </div>
            {entries.length === 0 ? (
                <div className="text-center py-8 text-muted-foreground">No {type} registered yet</div>
            ) : (
                <ScrollArea className="h-[400px] pr-4">
                    <div className="space-y-2">
                        {entries.map(([name, definition]) => (
                            <DefinitionCard
                                key={name}
                                name={name}
                                definition={definition}
                                blockType={type}
                                onUnregister={() => onUnregister(name)}
                            />
                        ))}
                    </div>
                </ScrollArea>
            )}
        </div>
    );
};

interface DefinitionCardProps {
    name: string;
    definition: UserDefinedDefinition;
    blockType: BlockType;
    onUnregister: () => void;
}

const DefinitionCard = ({ name, definition, blockType, onUnregister }: DefinitionCardProps) => {
    const router = useRouter();
    const [confirmDelete, setConfirmDelete] = useState(false);

    const handleEdit = () => {
        router.push(`/user-defined/${blockType}/edit?name=${encodeURIComponent(name)}`);
    };

    return (
        <Card className="py-3">
            <CardHeader className="py-0 px-4">
                <div className="flex items-center justify-between">
                    <div className="flex-1 min-w-0">
                        <CardTitle className="text-sm font-medium truncate">{name}</CardTitle>
                        {definition.description && (
                            <CardDescription className="text-xs truncate">{definition.description}</CardDescription>
                        )}
                    </div>
                    <div className="flex items-center gap-1 ml-2">
                        <Button variant="ghost" size="sm" onClick={handleEdit} className="text-xs">
                            Edit
                        </Button>
                        {confirmDelete ? (
                            <div className="flex items-center gap-1">
                                <Button variant="destructive" size="sm" onClick={onUnregister} className="text-xs">
                                    Confirm
                                </Button>
                                <Button
                                    variant="ghost"
                                    size="sm"
                                    onClick={() => setConfirmDelete(false)}
                                    className="text-xs"
                                >
                                    Cancel
                                </Button>
                            </div>
                        ) : (
                            <Button variant="ghost" size="sm" onClick={() => setConfirmDelete(true)}>
                                <TrashIcon className="size-4 text-destructive" />
                            </Button>
                        )}
                    </div>
                </div>
            </CardHeader>
        </Card>
    );
};

interface UserDefinedPageProps {
    initialTab?: BlockType;
}

const UserDefinedPage = ({ initialTab = "actions" }: UserDefinedPageProps) => {
    const router = useRouter();
    const activeTab = initialTab;
    const [definitions, setDefinitions] = useState<Record<BlockType, Record<string, UserDefinedDefinition>>>({
        actions: {},
        conditions: {},
        triggers: {},
        variables: {},
    });
    const [loading, setLoading] = useState<Record<BlockType, boolean>>({
        actions: false,
        conditions: false,
        triggers: false,
        variables: false,
    });

    const loadDefinitions = useCallback(async (type: BlockType) => {
        setLoading((prev) => ({ ...prev, [type]: true }));
        try {
            const data = await userDefinedApi.getAll(type);
            setDefinitions((prev) => ({ ...prev, [type]: data as Record<string, UserDefinedDefinition> }));
        } catch (error) {
            console.error(`Failed to load ${type}:`, error);
            toast.error(`Failed to load ${type}`);
        } finally {
            setLoading((prev) => ({ ...prev, [type]: false }));
        }
    }, []);

    const handleUnregister = useCallback(
        async (type: BlockType, name: string) => {
            try {
                await userDefinedApi.unregister(type, name);
                toast.success(`${type.slice(0, -1)} "${name}" unregistered successfully`);
                await loadDefinitions(type);
            } catch (error) {
                console.error(`Failed to unregister ${type}:`, error);
                toast.error(`Failed to unregister ${type.slice(0, -1)}`);
            }
        },
        [loadDefinitions]
    );

    useEffect(() => {
        loadDefinitions(activeTab);
    }, [activeTab, loadDefinitions]);

    return (
        <div className="p-4 sm:p-6 bg-gray-50 min-h-screen">
            <div className="max-w-4xl mx-auto">
                <header className="mb-6">
                    <h1 className="text-xl sm:text-2xl font-bold">User-Defined Block Types</h1>
                    <p className="text-sm text-gray-500">
                        Manage your custom actions, conditions, triggers, and variables.
                    </p>
                </header>

                <Card className="py-4">
                    <CardContent className="p-0 px-4">
                        <Tabs value={activeTab} onValueChange={(v) => router.push(`/user-defined/${v}`)}>
                            <TabsList className="grid w-full grid-cols-4">
                                {blockTypeConfigs.map((config) => (
                                    <TabsTrigger key={config.type} value={config.type} className="flex items-center gap-1">
                                        {config.icon}
                                        <span className="hidden sm:inline">{config.label}</span>
                                    </TabsTrigger>
                                ))}
                            </TabsList>

                            {blockTypeConfigs.map((config) => (
                                <TabsContent key={config.type} value={config.type} className="mt-4">
                                    <div className="space-y-4">
                                        <div className="flex items-center justify-between">
                                            <div>
                                                <h3 className="text-lg font-semibold">{config.label}</h3>
                                                <p className="text-sm text-muted-foreground">{config.description}</p>
                                            </div>
                                            <Button size="sm" onClick={() => router.push(`/user-defined/${config.type}/new`)}>
                                                <PlusIcon className="size-4 mr-1" />
                                                Register {config.singularLabel}
                                            </Button>
                                        </div>
                                        <DefinitionList
                                            type={config.type}
                                            definitions={definitions[config.type]}
                                            onRefresh={() => loadDefinitions(config.type)}
                                            onUnregister={(name) => handleUnregister(config.type, name)}
                                            isLoading={loading[config.type]}
                                        />
                                    </div>
                                </TabsContent>
                            ))}
                        </Tabs>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
};

export default UserDefinedPage;
