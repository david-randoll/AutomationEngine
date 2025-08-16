import React from "react";
import { Controller, useFormContext } from "react-hook-form";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import ModuleList from "@/components/ModuleList";
import ArrayOfPrimitives from "./ArrayOfPrimitives";
import ArrayOfObjects from "./ArrayOfObjects";
import { capitalize } from "@/lib/utils";

interface FieldRendererProps {
    fieldKey: string | number;
    schema: any;
    rootSchema: any;
    pathInData: (string | number)[];
    onAddBlock: (blockType: Area, pathInData: (string | number)[], targetIsArray: boolean) => void;
}

const FieldRenderer = ({ fieldKey, schema, rootSchema, pathInData, onAddBlock }: FieldRendererProps) => {
    const { control, getValues } = useFormContext();

    function resolveSchema(schema: any, rootSchema: any) {
        if (schema?.$ref) {
            const refPath = schema.$ref.replace(/^#\//, "").split("/");
            let resolved: any = rootSchema;
            for (const segment of refPath) resolved = resolved?.[segment];
            return resolved;
        }
        return schema;
    }

    const resolvedSch = resolveSchema(schema, rootSchema);
    const type = resolvedSch?.type || "string";
    const name = pathInData.join(".");
    const title = String(fieldKey);

    if (type === "array" && resolvedSch.items) {
        const itemsSchema = resolveSchema(resolvedSch.items, rootSchema);
        const itemsType = itemsSchema.type || "string";

        if (itemsSchema["x-block-type"]) {
            const blockType = itemsSchema["x-block-type"] as Area;

            return (
                <div key={name}>
                    <ModuleList title={capitalize(title)} path={pathInData} />
                    <button
                        className="inline-flex items-center px-3 py-1.5 border rounded text-sm bg-white hover:shadow mt-2"
                        onClick={() => onAddBlock(blockType, pathInData, true)}
                        type="button">
                        Add {capitalize(blockType)}
                    </button>
                </div>
            );
        }

        if (["string", "number", "boolean"].includes(itemsType)) {
            return (
                <ArrayOfPrimitives
                    key={name}
                    name={name}
                    title={title}
                    itemsType={itemsType as "string" | "number" | "boolean"}
                />
            );
        }

        if (itemsType === "object") {
            return (
                <ArrayOfObjects
                    key={name}
                    name={name}
                    title={title}
                    itemsSchema={itemsSchema}
                    pathInData={pathInData}
                    onAddBlock={onAddBlock}
                    rootSchema={rootSchema}
                />
            );
        }

        return (
            <div key={name}>
                <label>{capitalize(title)}</label>
                <Controller
                    control={control}
                    name={name}
                    render={({ field }) => (
                        <Textarea
                            rows={4}
                            {...field}
                            value={field.value ? JSON.stringify(field.value, null, 2) : ""}
                            onChange={(e) => {
                                try {
                                    field.onChange(JSON.parse(e.target.value));
                                } catch {
                                    field.onChange(e.target.value);
                                }
                            }}
                        />
                    )}
                />
            </div>
        );
    }

    if (type === "object" && resolvedSch["x-block-type"]) {
        const blockType = resolvedSch["x-block-type"] as Area;
        const val = getValues(name) ?? null;

        return (
            <div key={name} className="border rounded p-3 mb-4">
                <ModuleList title={capitalize(title)} path={pathInData} />
                {!val && (
                    <button
                        className="inline-flex items-center px-3 py-1.5 border rounded text-sm bg-white hover:shadow mt-2"
                        onClick={() => onAddBlock(blockType, pathInData, false)}
                        type="button">
                        Add {capitalize(blockType)}
                    </button>
                )}
            </div>
        );
    }

    if (type === "object" && resolvedSch.properties) {
        return (
            <div key={name} className="border p-3 rounded space-y-2 mb-4">
                <label className="block font-medium">{capitalize(title)}</label>
                {Object.entries(resolvedSch.properties).map(([childKey, childSchema]) => (
                    <FieldRenderer
                        key={`${name}.${childKey}`}
                        fieldKey={childKey}
                        schema={childSchema}
                        rootSchema={rootSchema}
                        pathInData={[...pathInData, childKey]}
                        onAddBlock={onAddBlock}
                    />
                ))}
            </div>
        );
    }

    if (type === "boolean") {
        return (
            <label key={name} className="inline-flex items-center space-x-2">
                <Controller
                    control={control}
                    name={name}
                    render={({ field }) => (
                        <input
                            type="checkbox"
                            {...field}
                            checked={Boolean(field.value)}
                            onChange={(e) => field.onChange(e.target.checked)}
                        />
                    )}
                />
                <span>{capitalize(title)}</span>
            </label>
        );
    }

    return (
        <div key={name}>
            <label className="block text-sm font-medium">{capitalize(title)}</label>
            <Controller
                control={control}
                name={name}
                render={({ field }) => (
                    <Input
                        {...field}
                        type="text"
                        onChange={(e) => field.onChange(e.target.value)}
                        value={field.value || ""}
                    />
                )}
            />
        </div>
    );
};

export default FieldRenderer;
