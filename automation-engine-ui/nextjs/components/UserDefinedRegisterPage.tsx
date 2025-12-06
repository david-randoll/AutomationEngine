"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useFormContext, useWatch } from "react-hook-form";
import ModuleEditor from "@/components/ModuleEditor";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { userDefinedApi } from "@/lib/user-defined-api";
import type { BlockType, UserDefinedDefinition } from "@/types/user-defined";
import type { ModuleType, Path } from "@/types/types";
import { toast } from "sonner";
import { ArrowLeftIcon, SaveIcon } from "lucide-react";
import { exportJson, exportYaml } from "@/utils/automation";
import CopyableBlock from "./CopyableBlock";

interface UserDefinedRegisterPageProps {
    blockType: BlockType;
}

const blockTypeLabels: Record<BlockType, string> = {
    actions: "Action",
    conditions: "Condition",
    triggers: "Trigger",
    variables: "Variable",
};

const UserDefinedRegisterPage = ({ blockType }: UserDefinedRegisterPageProps) => {
    const router = useRouter();
    const { setValue, getValues } = useFormContext();
    const rootPath: Path = ["register"];
    const pathKey = rootPath.join(".");

    const [schema, setSchema] = useState<ModuleType | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const formData = useWatch({ name: pathKey });
    const [jsonData, setJsonData] = useState("");
    const [yamlData, setYamlData] = useState("");

    useEffect(() => {
        setJsonData(exportJson(formData));
        setYamlData(exportYaml(formData));
    }, [formData]);

    useEffect(() => {
        // Reset the form when the component mounts
        setValue(pathKey, {});

        // Fetch the schema for the block type
        userDefinedApi
            .getSchema(blockType)
            .then((data) => {
                setSchema(data);
            })
            .catch((err) => {
                console.error("Failed to fetch schema:", err);
                setError("Failed to load schema");
            });
    }, [blockType, setValue, pathKey]);

    const handleSubmit = async () => {
        setError(null);
        const data = getValues(pathKey) as UserDefinedDefinition;

        if (!data?.name) {
            setError("Name is required");
            return;
        }

        setIsSubmitting(true);
        try {
            await userDefinedApi.register(blockType, data);
            toast.success(`${blockTypeLabels[blockType]} "${data.name}" registered successfully`);
            router.push("./user-defined");
        } catch (err) {
            console.error("Failed to register:", err);
            if (err instanceof Error) {
                setError(err.message);
            } else {
                setError("Failed to register");
            }
            toast.error(`Failed to register ${blockTypeLabels[blockType].toLowerCase()}`);
        } finally {
            setIsSubmitting(false);
        }
    };

    if (error && !schema) {
        return (
            <div className="p-4 sm:p-6 bg-gray-50 min-h-screen">
                <div className="max-w-7xl mx-auto">
                    <div className="text-center py-12">
                        <p className="text-destructive">{error}</p>
                        <Button variant="outline" className="mt-4" onClick={() => router.push("./user-defined")}>
                            <ArrowLeftIcon className="size-4 mr-2" />
                            Back to User-Defined Types
                        </Button>
                    </div>
                </div>
            </div>
        );
    }

    if (!schema) {
        return (
            <div className="p-4 sm:p-6 bg-gray-50 min-h-screen">
                <div className="max-w-7xl mx-auto">
                    <div className="text-center py-12">Loading schema...</div>
                </div>
            </div>
        );
    }

    return (
        <div className="p-4 sm:p-6 bg-gray-50 min-h-screen">
            <div className="max-w-7xl mx-auto">
                <header className="flex flex-col sm:flex-row items-start sm:items-center justify-between mb-6 gap-2 sm:gap-0">
                    <div className="flex items-center gap-4">
                        <Button variant="ghost" size="sm" onClick={() => router.push("/user-defined")}>
                            <ArrowLeftIcon className="size-4" />
                        </Button>
                        <div>
                            <h1 className="text-xl sm:text-2xl font-bold">
                                Register New {blockTypeLabels[blockType]}
                            </h1>
                            <p className="text-sm text-gray-500">
                                Create a new user-defined {blockTypeLabels[blockType].toLowerCase()}
                            </p>
                        </div>
                    </div>
                    <Button onClick={handleSubmit} disabled={isSubmitting}>
                        <SaveIcon className="size-4 mr-2" />
                        {isSubmitting ? "Registering..." : "Register"}
                    </Button>
                </header>

                {error && (
                    <div className="mb-4 p-3 bg-destructive/10 border border-destructive/20 rounded-md text-destructive text-sm">
                        {error}
                    </div>
                )}

                <main className="grid grid-cols-1 md:grid-cols-3 gap-4 md:gap-6">
                    <section className="md:col-span-2 space-y-4">
                        <ModuleEditor
                            module={{
                                schema: schema.schema,
                            }}
                            path={rootPath}
                        />
                    </section>

                    <aside className="md:col-span-1 space-y-4 flex flex-col h-[60vh] md:h-[calc(95vh-6rem)]">
                        <Card className="flex flex-col h-full">
                            <CardHeader>
                                <CardTitle className="text-lg font-semibold">Preview</CardTitle>
                            </CardHeader>

                            <CardContent className="flex-1 flex flex-col gap-2 overflow-hidden">
                                <div className="flex-1 min-h-0 overflow-auto">
                                    <CopyableBlock label="JSON" content={jsonData} language="json" />
                                </div>

                                <div className="flex-1 min-h-0 overflow-auto">
                                    <CopyableBlock label="YAML" content={yamlData} language="yaml" />
                                </div>
                            </CardContent>
                        </Card>
                    </aside>
                </main>
            </div>
        </div>
    );
};

export default UserDefinedRegisterPage;
