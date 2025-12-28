import { Controller, useFormContext } from "react-hook-form";
import { Info } from "lucide-react";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
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
import MonacoEditor from "@monaco-editor/react";
import type { Path, Area, ModuleType, JsonSchema } from "@/types/types";

interface FieldRendererProps {
    fieldKey: string | number;
    schema: JsonSchema;
    rootSchema: JsonSchema;
    pathInData: Path;
    onAddBlock: (blockType: Area, pathInData: Path, targetIsArray: boolean) => void;
}

const LabelWithHelp = ({ title, helpText }: { title: string; helpText?: string }) => (
    <div className="flex items-center gap-1.5">
        <label className="block text-sm font-medium">{capitalize(title)}</label>
        {helpText && (
            <Tooltip>
                <TooltipTrigger asChild>
                    <Info size={14} className="text-gray-400 cursor-help hover:text-gray-600 transition-colors" />
                </TooltipTrigger>
                <TooltipContent>
                    <p>{helpText}</p>
                </TooltipContent>
            </Tooltip>
        )}
    </div>
);

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
    const monacoOptions = resolvedSch?.["x-presentation-monaco-options"] as Record<string, unknown> | undefined;
    const customComponent = resolvedSch?.["x-presentation-custom-component"] as string | undefined;
    const readOnly = (resolvedSch?.["x-presentation-readonly"] ?? false) as boolean | undefined;
    const customProps = resolvedSch?.["x-presentation-custom-props"] as Record<string, unknown> | undefined;
    const minValue = resolvedSch?.["x-presentation-min"] as number | undefined ?? resolvedSch.minimum as number | undefined;
    const maxValue = resolvedSch?.["x-presentation-max"] as number | undefined ?? resolvedSch.maximum as number | undefined;

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
                ...resolvedSch,
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

    // ==================== WIDGET-SPECIFIC RENDERING ====================

    // CHECKBOX widget - for boolean values styled as checkbox
    if (presentationWidget === "checkbox" || (type === "boolean" && presentationWidget !== "switch")) {
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
                                className="rounded border-gray-300 text-blue-600 focus:ring-blue-500 w-4 h-4"
                            />
                        )}
                    />
                    <span className="text-sm font-medium">{capitalize(title)}</span>
                    {helpText && (
                        <Tooltip>
                            <TooltipTrigger asChild>
                                <Info size={14} className="text-gray-400 cursor-help hover:text-gray-600 transition-colors ml-1" />
                            </TooltipTrigger>
                            <TooltipContent>
                                <p>{helpText}</p>
                            </TooltipContent>
                        </Tooltip>
                    )}
                </label>
            </div>
        );
    }

    // SWITCH widget - toggle for boolean values
    if (presentationWidget === "switch") {
        return (
            <div key={name} className="space-y-1">
                <div className="flex items-center justify-between">
                    <LabelWithHelp title={title} helpText={helpText} />
                    <Controller
                        control={control}
                        name={name}
                        render={({ field }) => (
                            <label className="relative inline-flex items-center cursor-pointer">
                                <input
                                    type="checkbox"
                                    checked={Boolean(field.value)}
                                    onChange={(e) => field.onChange(e.target.checked)}
                                    disabled={readOnly}
                                    className="sr-only peer"
                                />
                                <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                            </label>
                        )}
                    />
                </div>
            </div>
        );
    }

    // NUMBER widget - numeric input with min/max
    if (presentationWidget === "number" || type === "number") {
        return (
            <div key={name} className="space-y-1">
                <LabelWithHelp title={title} helpText={helpText} />
                <Controller
                    control={control}
                    name={name}
                    render={({ field }) => (
                        <Input
                            {...field}
                            type="number"
                            placeholder={placeholder}
                            min={minValue}
                            max={maxValue}
                            disabled={readOnly}
                            onChange={(e) => field.onChange(parseFloat(e.target.value))}
                            value={field.value ?? ""}
                        />
                    )}
                />
            </div>
        );
    }

    // SLIDER widget - range slider for numeric values
    if (presentationWidget === "slider") {
        const min = minValue ?? 0;
        const max = maxValue ?? 100;

        return (
            <div key={name} className="space-y-2">
                <Controller
                    control={control}
                    name={name}
                    render={({ field }) => (
                        <>
                            <div className="flex justify-between items-center">
                                <LabelWithHelp title={title} helpText={helpText} />
                                <span className="text-sm text-gray-500">{field.value ?? min}</span>
                            </div>
                            <input
                                type="range"
                                min={min}
                                max={max}
                                step={1}
                                value={field.value ?? min}
                                onChange={(e) => field.onChange(parseInt(e.target.value))}
                                disabled={readOnly}
                                className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-blue-600"
                            />
                        </>
                    )}
                />
            </div>
        );
    }

    // DROPDOWN widget - select from options (checks annotation options first, then enum)
    if (presentationWidget === "dropdown" || (type === "string" && resolvedSch.enum)) {
        const options = dropdownOptions || (resolvedSch.enum as string[]) || [];
        const labels = dropdownLabels || options;

        return (
            <div key={name} className="space-y-1">
                <LabelWithHelp title={title} helpText={helpText} />
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
            </div>
        );
    }

    // RADIO widget - radio button group
    if (presentationWidget === "radio") {
        const options = dropdownOptions || (resolvedSch.enum as string[]) || [];
        const labels = dropdownLabels || options;

        return (
            <div key={name} className="space-y-2">
                <LabelWithHelp title={title} helpText={helpText} />
                <Controller
                    control={control}
                    name={name}
                    render={({ field }) => (
                        <div className="space-y-2">
                            {options.map((option: string, idx: number) => (
                                <label key={option} className="flex items-center space-x-2 cursor-pointer">
                                    <input
                                        type="radio"
                                        value={option}
                                        checked={field.value === option}
                                        onChange={() => field.onChange(option)}
                                        disabled={readOnly}
                                        className="w-4 h-4 text-blue-600 border-gray-300 focus:ring-blue-500"
                                    />
                                    <span className="text-sm">{labels[idx] || capitalize(option)}</span>
                                </label>
                            ))}
                        </div>
                    )}
                />
            </div>
        );
    }

    // TEXTAREA widget - multi-line text input
    if (presentationWidget === "textarea") {
        return (
            <div key={name} className="space-y-1">
                <LabelWithHelp title={title} helpText={helpText} />
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
            </div>
        );
    }

    // MONACO_EDITOR widget - code editor with syntax highlighting
    if (presentationWidget === "monaco_editor" && monacoLanguage) {
        return (
            <div key={name} className="space-y-1">
                <LabelWithHelp title={title} helpText={helpText} />
                <Controller
                    control={control}
                    name={name}
                    render={({ field }) => (
                        <div className="border rounded-md overflow-hidden">
                            <MonacoEditor
                                height="300px"
                                language={monacoLanguage}
                                value={field.value || ""}
                                onChange={(value) => field.onChange(value || "")}
                                options={{
                                    minimap: { enabled: false },
                                    scrollBeyondLastLine: false,
                                    fontSize: 13,
                                    lineNumbers: "on" as const,
                                    readOnly: readOnly || false,
                                    ...monacoOptions
                                }}
                            />
                        </div>
                    )}
                />
            </div>
        );
    }

    // DATE widget - date picker
    if (presentationWidget === "date") {
        return (
            <div key={name} className="space-y-1">
                <LabelWithHelp title={title} helpText={helpText} />
                <Controller
                    control={control}
                    name={name}
                    render={({ field }) => (
                        <Input
                            {...field}
                            type="date"
                            disabled={readOnly}
                            value={field.value || ""}
                        />
                    )}
                />
            </div>
        );
    }

    // TIME widget - time picker
    if (presentationWidget === "time") {
        return (
            <div key={name} className="space-y-1">
                <LabelWithHelp title={title} helpText={helpText} />
                <Controller
                    control={control}
                    name={name}
                    render={({ field }) => (
                        <Input
                            {...field}
                            type="time"
                            disabled={readOnly}
                            value={field.value || ""}
                        />
                    )}
                />
            </div>
        );
    }

    // DATETIME widget - date and time picker
    if (presentationWidget === "datetime") {
        return (
            <div key={name} className="space-y-1">
                <LabelWithHelp title={title} helpText={helpText} />
                <Controller
                    control={control}
                    name={name}
                    render={({ field }) => (
                        <Input
                            {...field}
                            type="datetime-local"
                            disabled={readOnly}
                            value={field.value || ""}
                        />
                    )}
                />
            </div>
        );
    }

    // COLOR widget - color picker
    if (presentationWidget === "color") {
        return (
            <div key={name} className="space-y-1">
                <LabelWithHelp title={title} helpText={helpText} />
                <Controller
                    control={control}
                    name={name}
                    render={({ field }) => (
                        <div className="flex items-center gap-2">
                            <input
                                type="color"
                                value={field.value || "#000000"}
                                onChange={(e) => field.onChange(e.target.value)}
                                disabled={readOnly}
                                className="w-20 h-10 border rounded cursor-pointer"
                            />
                            <Input
                                type="text"
                                value={field.value || ""}
                                onChange={(e) => field.onChange(e.target.value)}
                                placeholder="#000000"
                                disabled={readOnly}
                                className="flex-1"
                            />
                        </div>
                    )}
                />
            </div>
        );
    }

    // FILE widget - file upload input
    if (presentationWidget === "file") {
        return (
            <div key={name} className="space-y-1">
                <LabelWithHelp title={title} helpText={helpText} />
                <Controller
                    control={control}
                    name={name}
                    render={({ field: { value, onChange, ...field } }) => (
                        <Input
                            {...field}
                            type="file"
                            disabled={readOnly}
                            onChange={(e) => {
                                const file = e.target.files?.[0];
                                if (file) {
                                    onChange(file.name);
                                }
                            }}
                        />
                    )}
                />
            </div>
        );
    }

    // CUSTOM widget - user-registered custom component
    if (presentationWidget === "custom" && customComponent) {
        const CustomWidget = customWidgetRegistry.get(customComponent);

        if (CustomWidget) {
            return (
                <div key={name} className="space-y-1">
                    <LabelWithHelp title={title} helpText={helpText} />
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
                </div>
            );
        } else {
            console.warn(`Custom widget "${customComponent}" not found in registry`);
        }
    }

    // Default string input (fallback)
    return (
        <div key={name} className="space-y-1">
            <LabelWithHelp title={title} helpText={helpText} />
            <Controller
                control={control}
                name={name}
                render={({ field }) => (
                    <Input
                        {...field}
                        type="text"
                        placeholder={placeholder}
                        disabled={readOnly}
                        value={field.value || ""}
                    />
                )}
            />
        </div>
    );
};

export default FieldRenderer;
