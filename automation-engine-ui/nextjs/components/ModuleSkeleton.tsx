"use client";

interface ModuleSkeletonProps {
    numOfProps: number;
}

const ModuleSkeleton = ({ numOfProps }: ModuleSkeletonProps) => {
    return (
        <div className="rounded-md space-y-4 animate-pulse">
            {/* Header buttons */}
            <div className="mb-4 flex space-x-3">
                {Array.from({ length: 2 }).map((_, idx) => (
                    <div key={idx} className="h-8 w-24 bg-gray-200 rounded"></div>
                ))}
                <div className="ml-auto h-8 w-32 bg-gray-200 rounded"></div>
            </div>

            {/* Property rows */}
            {Array.from({ length: numOfProps }).map((_, idx) => (
                <div key={idx} className="space-y-1">
                    <div className="h-3 w-28 bg-gray-200 rounded"></div>
                    <div className="h-8 bg-gray-200 rounded w-full"></div>
                </div>
            ))}

            {/* Footer button */}
            <div className="flex justify-end">
                <div className="h-8 w-24 bg-gray-200 rounded"></div>
            </div>
        </div>
    );
};

export default ModuleSkeleton;
