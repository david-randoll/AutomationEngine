import type { ComponentType } from "react";
import type { Control, FieldValues } from "react-hook-form";

/**
 * Props that will be passed to custom widget components
 */
export interface CustomWidgetProps {
    /**
     * Field name for form registration
     */
    name: string;
    /**
     * Field value
     */
    value: unknown;
    /**
     * Callback to update the field value
     */
    onChange: (value: unknown) => void;
    /**
     * React Hook Form control object
     */
    control: Control<FieldValues>;
    /**
     * Field schema
     */
    schema: Record<string, unknown>;
    /**
     * Custom properties from @PresentationHint
     */
    customProps?: Record<string, unknown>;
    /**
     * Field label
     */
    label: string;
}

/**
 * Registry for custom widget components that can be used with @PresentationHint(widget=CUSTOM)
 */
class CustomWidgetRegistry {
    private widgets = new Map<string, ComponentType<CustomWidgetProps>>();

    /**
     * Register a custom widget component
     * @param name - The component name to reference in @PresentationHint(customComponent="name")
     * @param component - The React component to render
     */
    register(name: string, component: ComponentType<CustomWidgetProps>): void {
        this.widgets.set(name, component);
    }

    /**
     * Get a registered widget component by name
     * @param name - The component name
     * @returns The component or undefined if not found
     */
    get(name: string): ComponentType<CustomWidgetProps> | undefined {
        return this.widgets.get(name);
    }

    /**
     * Check if a widget is registered
     * @param name - The component name
     * @returns true if registered
     */
    has(name: string): boolean {
        return this.widgets.has(name);
    }

    /**
     * Unregister a widget
     * @param name - The component name
     */
    unregister(name: string): void {
        this.widgets.delete(name);
    }

    /**
     * Get all registered widget names
     * @returns Array of widget names
     */
    getRegisteredNames(): string[] {
        return Array.from(this.widgets.keys());
    }
}

// Singleton instance
export const customWidgetRegistry = new CustomWidgetRegistry();

/**
 * Utility function to register a widget (alternative to using the registry directly)
 * @param name - The component name
 * @param component - The React component
 */
export function registerCustomWidget(
    name: string,
    component: ComponentType<CustomWidgetProps>
): void {
    customWidgetRegistry.register(name, component);
}
