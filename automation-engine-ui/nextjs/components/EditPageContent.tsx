"use client";

import { useSearchParams } from "next/navigation";
import { useRouter } from "next/navigation";
import { useEffect } from "react";
import type { BlockType } from "@/types/user-defined";
import UserDefinedEditPage from "@/components/UserDefinedEditPage";

interface EditPageContentProps {
    blockType: BlockType;
}

export default function EditPageContent({ blockType }: EditPageContentProps) {
    const searchParams = useSearchParams();
    const router = useRouter();
    const name = searchParams.get("name");

    useEffect(() => {
        if (!name) {
            router.push(`/user-defined/${blockType}`);
        }
    }, [name, blockType, router]);

    if (!name) {
        return (
            <div className="p-4 sm:p-6 bg-gray-50 min-h-screen">
                <div className="max-w-7xl mx-auto">
                    <div className="text-center py-12">Redirecting...</div>
                </div>
            </div>
        );
    }

    return <UserDefinedEditPage blockType={blockType} name={name} />;
}
