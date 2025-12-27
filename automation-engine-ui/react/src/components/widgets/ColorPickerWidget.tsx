import type { CustomWidgetProps } from "@/lib/CustomWidgetRegistry";
import { registerCustomWidget } from "@/lib/CustomWidgetRegistry";
import { Input } from "@/components/ui/input";

/**
 * Example custom widget that renders a color picker input.
 * This demonstrates how to create and register a custom widget component.
 */
function ColorPickerWidget({ value, onChange }: CustomWidgetProps) {
    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        onChange(e.target.value);
    };

    return (
        <div className="flex items-center gap-2">
            <input
                type="color"
                value={value as string || "#000000"}
                onChange={handleChange}
                className="h-10 w-20 rounded border cursor-pointer"
            />
            <Input
                type="text"
                value={value as string || ""}
                onChange={handleChange}
                placeholder="#000000"
                className="flex-1"
            />
        </div>
    );
}

// Register the widget so it can be used with @PresentationHint(customComponent="colorPicker")
registerCustomWidget("colorPicker", ColorPickerWidget);

export default ColorPickerWidget;
