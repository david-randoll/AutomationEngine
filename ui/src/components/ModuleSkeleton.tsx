import React from "react";

interface ModuleSkeletonProps {
    numOfProps: number;
}

const ModuleSkeleton = ({ numOfProps }: ModuleSkeletonProps) => {
    return (
        <div className="rounded-md space-y-4 animate-pulse">
            <div className="flex justify-between items-center">
                <div className="mb-4 flex space-x-3">
                    <div className="h-6 w-20 bg-gray-200 rounded"></div>
                    <div className="h-6 w-20 bg-gray-200 rounded"></div>
                </div>
                <div className="h-5 w-32 bg-gray-200 rounded"></div>
            </div>

            {Array.from({ length: numOfProps }).map((_, idx) => (
                <div key={idx} className="space-y-1">
                    <div className="h-3 w-28 bg-gray-200 rounded"></div>
                    <div className="h-8 bg-gray-200 rounded w-full"></div>
                </div>
            ))}

            <div className="flex justify-end">
                <div className="h-8 w-24 bg-gray-200 rounded"></div>
            </div>
        </div>
    );
};

export default ModuleSkeleton;
