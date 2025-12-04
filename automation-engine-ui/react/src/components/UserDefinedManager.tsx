import { useState, useEffect, useCallback } from "react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";
import { userDefinedApi } from "@/lib/user-defined-api";
import type { BlockType, UserDefinedDefinition } from "@/types/user-defined";
import { toast } from "sonner";
import { PlusIcon, TrashIcon, RefreshCwIcon, CodeIcon, ZapIcon, FilterIcon, VariableIcon } from "lucide-react";

interface BlockTypeConfig {
    type: BlockType;
    label: string;
    description: string;
    icon: React.ReactNode;
    exampleJson: string;
}

const blockTypeConfigs: BlockTypeConfig[] = [
    {
        type: "actions",
        label: "Actions",
        description: "User-defined reusable actions",
        icon: <ZapIcon className="size-4" />,
        exampleJson: JSON.stringify(
            {
                name: "myAction",
                description: "A custom action",
                parameters: { param1: "default" },
                variables: [],
                conditions: [],
                actions: [],
            },
            null,
            2
        ),
    },
    {
        type: "conditions",
        label: "Conditions",
        description: "User-defined reusable conditions",
        icon: <FilterIcon className="size-4" />,
        exampleJson: JSON.stringify(
            {
                name: "myCondition",
                description: "A custom condition",
                parameters: { param1: "default" },
                variables: [],
                conditions: [],
            },
            null,
            2
        ),
    },
    {
        type: "triggers",
        label: "Triggers",
        description: "User-defined reusable triggers",
        icon: <CodeIcon className="size-4" />,
        exampleJson: JSON.stringify(
            {
                name: "myTrigger",
                description: "A custom trigger",
                parameters: { param1: "default" },
                variables: [],
                triggers: [],
            },
            null,
            2
        ),
    },
    {
        type: "variables",
        label: "Variables",
        description: "User-defined reusable variables",
        icon: <VariableIcon className="size-4" />,
        exampleJson: JSON.stringify(
            {
                name: "myVariable",
                description: "A custom variable",
                parameters: { param1: "default" },
                variables: [],
            },
            null,
            2
        ),
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
                <div className="text-center py-8 text-muted-foreground">
                    No {type} registered yet
                </div>
            ) : (
                <ScrollArea className="h-[400px] pr-4">
                    <div className="space-y-2">
                        {entries.map(([name, definition]) => (
                            <DefinitionCard
                                key={name}
                                name={name}
                                definition={definition}
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
    onUnregister: () => void;
}

const DefinitionCard = ({ name, definition, onUnregister }: DefinitionCardProps) => {
    const [expanded, setExpanded] = useState(false);
    const [confirmDelete, setConfirmDelete] = useState(false);

    return (
        <Card className="py-3">
            <CardHeader className="py-0 px-4">
                <div className="flex items-center justify-between">
                    <div className="flex-1 min-w-0">
                        <CardTitle className="text-sm font-medium truncate">{name}</CardTitle>
                        {definition.description && (
                            <CardDescription className="text-xs truncate">
                                {definition.description}
                            </CardDescription>
                        )}
                    </div>
                    <div className="flex items-center gap-1 ml-2">
                        <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => setExpanded(!expanded)}
                            className="text-xs"
                        >
                            {expanded ? "Hide" : "View"}
                        </Button>
                        {confirmDelete ? (
                            <div className="flex items-center gap-1">
                                <Button
                                    variant="destructive"
                                    size="sm"
                                    onClick={onUnregister}
                                    className="text-xs"
                                >
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
                            <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => setConfirmDelete(true)}
                            >
                                <TrashIcon className="size-4 text-destructive" />
                            </Button>
                        )}
                    </div>
                </div>
            </CardHeader>
            {expanded && (
                <CardContent className="py-2 px-4">
                    <pre className="text-xs bg-muted p-2 rounded-md overflow-auto max-h-60">
                        {JSON.stringify(definition, null, 2)}
                    </pre>
                </CardContent>
            )}
        </Card>
    );
};

interface RegisterDialogProps {
    config: BlockTypeConfig;
    onRegister: (definition: UserDefinedDefinition) => Promise<void>;
}

const RegisterDialog = ({ config, onRegister }: RegisterDialogProps) => {
    const [open, setOpen] = useState(false);
    const [json, setJson] = useState(config.exampleJson);
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleRegister = async () => {
        setError(null);
        try {
            const parsed = JSON.parse(json);
            if (!parsed.name) {
                setError("Name is required");
                return;
            }
            setIsSubmitting(true);
            await onRegister(parsed);
            setOpen(false);
            setJson(config.exampleJson);
        } catch (e) {
            if (e instanceof SyntaxError) {
                setError("Invalid JSON format");
            } else if (e instanceof Error) {
                setError(e.message);
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button size="sm">
                    <PlusIcon className="size-4 mr-1" />
                    Register {config.label.slice(0, -1)}
                </Button>
            </DialogTrigger>
            <DialogContent className="max-w-2xl">
                <DialogHeader>
                    <DialogTitle>Register New {config.label.slice(0, -1)}</DialogTitle>
                    <DialogDescription>
                        Enter the JSON definition for your user-defined {config.type.slice(0, -1)}.
                    </DialogDescription>
                </DialogHeader>
                <div className="space-y-3">
                    <Textarea
                        value={json}
                        onChange={(e) => setJson(e.target.value)}
                        className="font-mono text-sm min-h-[300px]"
                        placeholder="Enter JSON definition..."
                    />
                    {error && (
                        <p className="text-sm text-destructive">{error}</p>
                    )}
                </div>
                <DialogFooter>
                    <Button variant="outline" onClick={() => setOpen(false)}>
                        Cancel
                    </Button>
                    <Button onClick={handleRegister} disabled={isSubmitting}>
                        {isSubmitting ? "Registering..." : "Register"}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

const UserDefinedManager = () => {
    const [activeTab, setActiveTab] = useState<BlockType>("actions");
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

    const handleRegister = useCallback(
        async (type: BlockType, definition: UserDefinedDefinition) => {
            try {
                await userDefinedApi.register(type, definition);
                toast.success(`${type.slice(0, -1)} "${definition.name}" registered successfully`);
                await loadDefinitions(type);
            } catch (error) {
                console.error(`Failed to register ${type}:`, error);
                toast.error(`Failed to register ${type.slice(0, -1)}`);
                throw error;
            }
        },
        [loadDefinitions]
    );

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
                        <Tabs value={activeTab} onValueChange={(v) => setActiveTab(v as BlockType)}>
                            <TabsList className="grid w-full grid-cols-4">
                                {blockTypeConfigs.map((config) => (
                                    <TabsTrigger
                                        key={config.type}
                                        value={config.type}
                                        className="flex items-center gap-1"
                                    >
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
                                                <p className="text-sm text-muted-foreground">
                                                    {config.description}
                                                </p>
                                            </div>
                                            <RegisterDialog
                                                config={config}
                                                onRegister={(def) => handleRegister(config.type, def)}
                                            />
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

export default UserDefinedManager;
