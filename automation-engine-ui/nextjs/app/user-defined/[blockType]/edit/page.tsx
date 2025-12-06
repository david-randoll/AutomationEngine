import { notFound } from "next/navigation";
import { Suspense } from "react";
import NavBar from "@/components/NavBar";
import type { BlockType } from "@/types/user-defined";
import EditPageContent from "@/components/EditPageContent";

const validBlockTypes = ["actions", "conditions", "triggers", "variables"] as const;

function isValidBlockType(type: string): type is BlockType {
    return (validBlockTypes as readonly string[]).includes(type);
}

export function generateStaticParams() {
    return validBlockTypes.map((blockType) => ({
        blockType,
    }));
}

export default async function EditUserDefinedPage({
    params,
}: Readonly<{
    params: Promise<{ blockType: string }>;
}>) {
    const { blockType } = await params;

    if (!isValidBlockType(blockType)) {
        notFound();
    }

    return (
        <div className="min-h-screen bg-background">
            <NavBar />
            <Suspense
                fallback={
                    <div className="p-4 sm:p-6 bg-gray-50 min-h-screen">
                        <div className="max-w-7xl mx-auto">
                            <div className="text-center py-12">Loading...</div>
                        </div>
                    </div>
                }
            >
                <EditPageContent blockType={blockType} />
            </Suspense>
        </div>
    );
}
