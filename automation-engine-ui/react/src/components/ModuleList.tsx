import { useFieldArray, useFormContext } from "react-hook-form";
import ModuleListItem from "./ModuleListItem";
import { Accordion } from "@/components/ui/accordion";
import type { Path } from "@/types/types";

interface ModuleListProps {
    title: string;
    path: Path;
}

const ModuleList = ({ title, path }: ModuleListProps) => {
    const { control } = useFormContext();
    const fieldName = path.join(".");

    const { fields, remove } = useFieldArray({
        control,
        name: fieldName,
        keyName: "reactHookFormId",
    });

    return (
        <div className="space-y-3">
            <div className="font-semibold text-lg">{title}</div>

            {fields.length === 0 && <div className="text-sm text-gray-500">No {title.toLowerCase()} yet.</div>}

            <Accordion type="single" collapsible className="space-y-2">
                {fields.map((field, index) => (
                    <ModuleListItem
                        key={field.reactHookFormId}
                        path={[...path, index]}
                        onRemove={() => remove(index)}
                    />
                ))}
            </Accordion>
        </div>
    );
};

export default ModuleList;
