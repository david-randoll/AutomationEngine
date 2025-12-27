import { Controller, useFormContext } from "react-hook-form";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import ModuleList from "@/components/ModuleList";
import ArrayOfPrimitives from "./ArrayOfPrimitives";
import ArrayOfObjects from "./ArrayOfObjects";
import { capitalize } from "@/lib/utils";
import ModuleListItem from "./ModuleListItem";
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "./ui/accordion";
import { Button } from "./ui/button";
import ModuleEditor from "./ModuleEditor";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { customWidgetRegistry } from "@/lib/CustomWidgetRegistry";
import type { Path, Area, ModuleType, JsonSchema } from "@/types/types";

interface FieldRendererProps {
    fieldKey: string | number;
    schema: JsonSchema;
    rootSchema: JsonSchema;
    pathInData: Path;
    onAddBlock: (blockType: Area, pathInData: Path, targetIsArray: boolean) => void;
}

const FieldRenderer = ({ fieldKey, schema, rootSchema, pathInData, onAddBlock }: FieldRendererProps) => {
    const { control, getValues, resetField } = useFormContext();

    function resolveSchema(schema: JsonSchema, rootSchema: JsonSchema): JsonSchema {
        if (schema?.$ref) {
            const refPath = (schema.$ref as string).replace(/^#\//, "").split("/");
            let resolved: JsonSchema = rootSchema;
            for (const segment of refPath) resolved = resolved?.[segment] as JsonSchema;
            return resolved;
        }
        return schema;
    }

    const resolvedSch = resolveSchema(schema, rootSchema);
    const type = resolvedSch?.type || "string";
    const name = pathInData.join(".");
    const title = String(fieldKey);

    // Extract presentation hints from schema
    const presentationWidget = resolvedSch?.["x-presentation-widget"] as string | undefined;
    const placeholder = resolvedSch?.["x-presentation-placeholder"] as string | undefined;
    const helpText = resolvedSch?.["x-presentation-help"] as string | undefined;
    const dropdownOptions = resolvedSch?.["x-presentation-dropdown-options"] as string[] | undefined;
    const dropdownLabels = resolvedSch?.["x-presentation-dropdown-labels"] as string[] | undefined;
    const monacoLanguage = resolvedSch?.["x-presentation-monaco-language"] as string | undefined;
    const customComponent = resolvedSch?.["x-presentation-custom-component"] as string | undefined;
    const readOnly = resolvedSch?.["x-presentation-readonly"] as boolean | undefined;
    const validation = resolvedSch?.["x-presentation-validation"] as Record<string, unknown> | undefined;
    const customProps = resolvedSch?.["x-presentation-custom-props"] as Record<string, unknown> | undefined;

    if (type === "array" && resolvedSch.items) {
        const itemsSchema = resolveSchema(resolvedSch.items as JsonSchema, rootSchema);
        const itemsType = itemsSchema.type || "string";

        if (itemsSchema["x-block-type"]) {
            const blockType = itemsSchema["x-block-type"] as Area;

            return (
                <div key={name}>
                    <ModuleList title={capitalize(title)} path={pathInData} />
                    <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        className="mt-2"
                        onClick={() => onAddBlock(blockType, pathInData, true)}>
                        + Add {capitalize(blockType)}
                    </Button>
                </div>
            );
        }

        if (["string", "number", "boolean"].includes(itemsType as string)) {
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
            <Accordion type="single" collapsible className="space-y-2">
                <div className="font-semibold text-lg">{capitalize(title)}</div>
                {val && <ModuleListItem key={name} path={pathInData} onRemove={() => resetField(name)} />}
                {!val && (
                    <>
                        <div className="text-sm text-gray-500">No {title.toLowerCase()} yet.</div>
                        <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            className="mt-2"
                            onClick={() => onAddBlock(blockType, pathInData, false)}>
                            + Add {capitalize(blockType)}
                        </Button>
                    </>
                )}
            </Accordion>
        );
    }

    if (type === "object" && resolvedSch.properties) {
        const module = {
            schema: {
                ...rootSchema,
                ...resolvedSch, // override the root schema with item schema
            },
        } as ModuleType;
        return (
            <Accordion type="single" collapsible className="w-full">
                <AccordionItem
                    key={pathInData.join(".")}
                    value={pathInData.join(".")}
                    className="border rounded-lg shadow-sm">
                    <AccordionTrigger className="flex items-center justify-between px-4 py-3 font-medium hover:text-blue-600 transition-colors">
                        <span>{capitalize(title)}</span>
                    </AccordionTrigger>

                    <AccordionContent className="px-4 pb-4 mt-2 space-y-3">
                        <ModuleEditor module={module} path={pathInData} />
                    </AccordionContent>
                </AccordionItem>
            </Accordion>
        );
    }

    if (type === "object") {
        // there is no properties to render so we'll render back ModuleEditor with a custom schema
        // that'll allow us to add additional properties
        const additionalPropSchema: ModuleType = {
            schema: {
                additionalProperties: {},
            },
        };
        return (
            <Accordion type="single" collapsible className="w-full">
                <AccordionItem key={name} value={name} className="border rounded-lg shadow-sm">
                    <AccordionTrigger className="flex items-center justify-between px-4 py-3 font-medium hover:text-blue-600 transition-colors">
                        {capitalize(title)}
                    </AccordionTrigger>
                    <AccordionContent className="px-4 pb-4 mt-2 space-y-3">
                        <ModuleEditor path={pathInData} module={additionalPropSchema} />
                    </AccordionContent>
                </AccordionItem>
            </Accordion>
        );
    }

    if (type === "boolean") {
        return (
            <div key={name} className="space-y-1">
                <label className="inline-flex items-center space-x-2">
                    <Controller
                        control={control}
                        name={name}
                        render={({ field }) => (
                            <input
                                type="checkbox"
                                {...field}
                                checked={Boolean(field.value)}
                                onChange={(e) => field.onChange(e.target.checked)}
                                disabled={readOnly}
                                className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                            />
                        )}
                    />
                    <span className="text-sm font-medium">{capitalize(title)}</span>
                </label>
                {helpText && <p className="text-xs text-gray-500 ml-6">{helpText}</p>}
            </div>
        );
    }

    if (type === "number") {
        const min = validation?.min as number | undefined ?? resolvedSch.minimum as number | undefined;
        const max = validation?.max as number | undefined ?? resolvedSch.maximum as number | undefined;
        
        return (
            <div key={name} className="space-y-1">
                <label className="block text-sm font-medium">{capitalize(title)}</label>
                <Controller
                    control={control}
                    name={name}
                    render={({ field }) => (
                        <Input
                            {...field}
                            type="number"
                            placeholder={placeholder}
                            min={min}
                            max={max}
                            disabled={readOnly}
                            onChange={(e) => field.onChange(parseFloat(e.target.value))}
                            value={field.value ?? ""}
                        />
                    )}
                />
                {helpText && <p className="text-xs text-gray-500">{helpText}</p>}
            </div>
        );
    }

    // Handle dropdowns - either from presentation hint or enum
    if (presentationWidget === "dropdown" || (type === "string" && resolvedSch.enum)) {
        const options = dropdownOptions || (resolvedSch.enum as string[]) || [];
        const labels = dropdownLabels || options;
        
        return (
            <div key={name} className="space-y-1">
                <label className="block text-sm font-medium">{capitalize(title)}</label>
                <Controller
                    control={control}
                    name={name}
                    render={({ field }) => (
                        <Select 
                            value={field.value || ""} 
                            onValueChange={(value) => field.onChange(value)}
                            disabled={readOnly}
                        >
                            <SelectTrigger className="w-full">
                                <SelectValue placeholder={placeholder || `Select ${capitalize(title)}`} />
                            </SelectTrigger>
                            <SelectContent>
                                {options.map((option: string, idx: number) => (
                                    <SelectItem key={option} value={option}>
                                        {labels[idx] || capitalize(option)}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    )}
                />
                {helpText && <p className="text-xs text-gray-500">{helpText}</p>}
            </div>
        );
    }

    // Handle custom widgets
    if (presentationWidget === "custom" && customComponent) {
        const CustomWidget = customWidgetRegistry.get(customComponent);
        
        if (CustomWidget) {
            return (
                <div key={name} className="space-y-1">
                    <label className="block text-sm font-medium">{capitalize(title)}</label>
                    <Controller
                        control={control}
                        name={name}
                        render={({ field }) => (
                            <CustomWidget
                                name={name}
                                value={field.value}
                                onChange={field.onChange}
                                control={control}
                                schema={resolvedSch}
                                customProps={customProps}
                                label={capitalize(title)}
                            />
                        )}
                    />
                    {helpText && <p className="text-xs text-gray-500">{helpText}</p>}
                </div>
            );
        } else {
            console.warn(`Custom widget "${customComponent}" not found in registry`);
        }
    }

    // Handle textarea widget
    if (presentationWidget === "textarea") {
        return (
            <div key={name} className="space-y-1">
                <label className="block text-sm font-medium">{capitalize(title)}</label>
                <Controller
                    control={control}
                    name={name}
                    render={({ field }) => (
                        <Textarea
                            {...field}
                            placeholder={placeholder}
                            disabled={readOnly}
                            rows={4}
                            value={field.value || ""}
                        />
                    )}
                />
                {helpText && <p className="text-xs text-gray-500">{helpText}</p>}
            </div>
        );
    }

    // Handle Monaco editor widget
    if (presentationWidget === "monaco_editor" && monacoLanguage) {
        // TODO: Implement Monaco editor integration
        // For now, fall back to textarea with a note
        return (
            <div key={name} className="space-y-1">
                <label className="block text-sm font-medium">
                    {capitalize(title)}
                    <span className="ml-2 text-xs text-gray-400">(Monaco editor - coming soon, using textarea)</span>
                </label>
                <Controller
                    control={control}
                    name={name}
                    render={({ field }) => (
                        <Textarea
                            {...field}
                            placeholder={placeholder || `Enter ${monacoLanguage} code`}
                            disabled={readOnly}
                            rows={10}
                            value={field.value || ""}
                            className="font-mono text-sm"
                        />
                    )}
                />
                {helpText && <p className="text-xs text-gray-500">{helpText}</p>}
            </div>
        );
    }

    // Default string input (fallback)
    return (
        <div key={name} className="space-y-1">
            <label className="block text-sm font-medium">{capitalize(title)}</label>
            <Controller
                control={control}
                name={name}
                render={({ field }) => (
                    <Input
                        {...field}
                        type="text"
                        placeholder={placeholder}
                        disabled={readOnly}
                        onChange={(e) => field.onChange(e.target.value)}
                        value={field.value || ""}
                    />
                )}
            />
            {helpText && <p className="text-xs text-gray-500">{helpText}</p>}
        </div>
    );
};

export default FieldRenderer;
