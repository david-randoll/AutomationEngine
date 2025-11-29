
import { useFormContext, useFieldArray, useWatch } from "react-hook-form";
import { Accordion, AccordionItem, AccordionTrigger, AccordionContent } from "@/components/ui/accordion";
import { Button } from "@/components/ui/button";
import { FaTrash } from "react-icons/fa";
import { capitalize } from "@/lib/utils";
import ModuleEditor from "./ModuleEditor";
import { useAutomationEngine } from "@/providers/AutomationEngineProvider";
import type { Path, ModuleType } from "@/types/types";

const ArrayOfObjects = ({
    name,
    title,
    itemsSchema,
    pathInData,
    rootSchema,
}: {
    name: string;
    title: string;
    itemsSchema: any;
    pathInData: Path;
    rootSchema: any;
}) => {
    const { evictSchema } = useAutomationEngine();
    const { control } = useFormContext();
    const { fields, append, remove } = useFieldArray({ control, name });
    const values = useWatch({ control, name }) || [];

    const module = {
        schema: {
            ...rootSchema,
            ...itemsSchema, // override the root schema with item schema
        },
    } as ModuleType;

    const handleRemove = (index: number, path: Path) => {
        evictSchema(path.join("."));
        remove(index);
    };

    return (
        <div className="space-y-3">
            <label className="block font-medium text-lg">{capitalize(title)}</label>

            <Accordion type="single" collapsible className="w-full">
                {fields.map((field, index) => {
                    const item = values[index];
                    const path = [...pathInData, index];

                    return (
                        <AccordionItem key={field.id} value={`${index}`} className="border rounded-lg shadow-sm">
                            <AccordionTrigger className="flex items-center justify-between px-4 py-3 font-medium hover:text-blue-600 transition-colors">
                                <span>
                                    {item?.alias || item?.label || item?.name || `${capitalize(title)} ${index + 1}`}
                                </span>
                            </AccordionTrigger>

                            <AccordionContent className="px-4 pb-4 mt-2 space-y-3">
                                <ModuleEditor module={module} path={path} />
                                <div className="flex justify-end mt-4">
                                    <Button
                                        variant="outline"
                                        size="sm"
                                        className="flex items-center gap-1 px-2 py-1 text-red-700"
                                        onClick={() => handleRemove(index, path)}>
                                        <FaTrash /> Delete
                                    </Button>
                                </div>
                            </AccordionContent>
                        </AccordionItem>
                    );
                })}
            </Accordion>

            <Button
                type="button"
                variant="outline"
                size="sm"
                className="mt-2"
                onClick={() => {
                    const newItem: any = {};
                    Object.entries(itemsSchema.properties || {}).forEach(([k]) => {
                        newItem[k] = null;
                    });
                    append(newItem);
                }}>
                + Add {capitalize(title)}
            </Button>
        </div>
    );
};

export default ArrayOfObjects;
