import { notFound } from "next/navigation";
import NavBar from "@/components/NavBar";
import UserDefinedPage from "@/components/UserDefinedPage";
import type { BlockType } from "@/types/user-defined";

const validBlockTypes = ["actions", "conditions", "triggers", "variables"] as const;

function isValidBlockType(type: string): type is BlockType {
    return (validBlockTypes as readonly string[]).includes(type);
}

export function generateStaticParams() {
    return validBlockTypes.map((blockType) => ({
        blockType,
    }));
}

export default async function UserDefinedBlockTypePage({
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
            <UserDefinedPage initialTab={blockType} />
        </div>
    );
}
