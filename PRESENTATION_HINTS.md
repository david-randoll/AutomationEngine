# Presentation Hints for Context Fields

This guide explains how to use the `@PresentationHint` annotation to customize how context fields are rendered in the AutomationEngine UI and other presentation layers.

## Overview

The `@PresentationHint` annotation allows you to specify how fields should be displayed without tying your code to any specific UI technology. The annotation is processed during JSON Schema generation and the metadata is made available to presentation layers through custom schema properties.

## Key Features

- **Generic and Technology-Agnostic**: Not tied to any specific UI framework
- **Optional**: Fields without annotations use default rendering based on type
- **Backwards Compatible**: Existing context classes work without modification
- **Extensible**: Supports custom widgets through a component registry

## Basic Usage

### Simple Field Customization

```java
import com.davidrandoll.automation.spi.annotation.PresentationHint;

public class MyActionContext implements IActionContext {
    // Textarea instead of single-line input
    @PresentationHint(
        widget = PresentationHint.Widget.TEXTAREA,
        placeholder = "Enter your message here",
        helpText = "This message supports template expressions like {{ event.data }}"
    )
    private String message;
    
    // Dropdown with custom options
    @PresentationHint(
        widget = PresentationHint.Widget.DROPDOWN,
        dropdownOptions = {"low", "medium", "high"},
        dropdownLabels = {"Low Priority", "Medium Priority", "High Priority"}
    )
    private String priority;
    
    // Number field with constraints
    @PresentationHint(
        widget = PresentationHint.Widget.NUMBER,
        min = 0,
        max = 100,
        helpText = "Enter a percentage value between 0 and 100"
    )
    private int percentage;
}
```

## Available Widgets

### AUTO (Default)
Automatically determines widget based on field type:
- `String` → Text input
- `int`, `long`, `double` → Number input  
- `boolean` → Checkbox
- `Enum` → Dropdown (using enum values)

```java
@PresentationHint(widget = PresentationHint.Widget.AUTO)
private String name; // Uses default text input
```

### TEXTAREA
Multi-line text input for longer content.

```java
@PresentationHint(
    widget = PresentationHint.Widget.TEXTAREA,
    placeholder = "Enter detailed description",
    helpText = "Supports markdown formatting"
)
private String description;
```

### NUMBER
Numeric input with optional min/max constraints.

```java
@PresentationHint(
    widget = PresentationHint.Widget.NUMBER,
    min = 1,
    max = 10,
    placeholder = "1-10"
)
private int priority;
```

### CHECKBOX
Checkbox for boolean values (default for boolean fields).

```java
@PresentationHint(
    widget = PresentationHint.Widget.CHECKBOX,
    helpText = "Enable debug logging"
)
private boolean debug;
```

### DROPDOWN
Select dropdown with custom options or enum values.

```java
// With custom options
@PresentationHint(
    widget = PresentationHint.Widget.DROPDOWN,
    dropdownOptions = {"json", "xml", "yaml"},
    dropdownLabels = {"JSON Format", "XML Format", "YAML Format"}
)
private String format;

// Enum fields automatically use dropdown if no annotation
private HttpMethod method; // Auto-renders as dropdown with GET, POST, PUT, DELETE, etc.
```

### MONACO_EDITOR
Code editor with syntax highlighting (currently falls back to styled textarea).

```java
@PresentationHint(
    widget = PresentationHint.Widget.MONACO_EDITOR,
    monacoLanguage = "javascript",
    monacoOptions = "{\"minimap\": {\"enabled\": false}, \"lineNumbers\": \"on\"}",
    helpText = "Enter JavaScript code"
)
private String scriptCode;
```

**Supported Languages**: `javascript`, `typescript`, `java`, `python`, `yaml`, `json`, `xml`, `sql`, `html`, `css`, `markdown`

### CUSTOM
Use a custom React component registered in the widget registry.

```java
@PresentationHint(
    widget = PresentationHint.Widget.CUSTOM,
    customComponent = "colorPicker",
    customProps = "{\"format\": \"hex\", \"showAlpha\": true}"
)
private String backgroundColor;
```

## Advanced Features

### Validation Hints

Add client-side validation hints to supplement Jakarta validation:

```java
@PresentationHint(
    pattern = "^[A-Z]{2,3}$",
    validationMessage = "Must be 2-3 uppercase letters",
    required = true
)
private String countryCode;
```

**Note**: Jakarta validation annotations (`@NotNull`, `@Min`, `@Max`, `@Pattern`) are automatically included in the schema and should be your primary validation mechanism.

### Read-Only Fields

```java
@PresentationHint(
    readOnly = true,
    helpText = "This field is automatically generated"
)
private String generatedId;
```

### Field Ordering

```java
@PresentationHint(order = 1)
private String firstName;

@PresentationHint(order = 2)
private String lastName;
```

**Note**: `@JsonPropertyOrder` takes precedence over the `order` property.

## Creating Custom Widgets

### Step 1: Create Widget Component

Create a React component in `automation-engine-ui/react/src/components/widgets/`:

```typescript
import { CustomWidgetProps, registerCustomWidget } from "@/lib/CustomWidgetRegistry";
import { Input } from "@/components/ui/input";

function ColorPickerWidget({ name, value, onChange, label, customProps }: CustomWidgetProps) {
    return (
        <div className="flex items-center gap-2">
            <input
                type="color"
                value={value as string || "#000000"}
                onChange={(e) => onChange(e.target.value)}
                className="h-10 w-20 rounded border cursor-pointer"
            />
            <Input
                type="text"
                value={value as string || ""}
                onChange={(e) => onChange(e.target.value)}
                placeholder="#000000"
                className="flex-1"
            />
        </div>
    );
}

// Register the widget
registerCustomWidget("colorPicker", ColorPickerWidget);

export default ColorPickerWidget;
```

### Step 2: Import Widget in App

Import your widget component so it registers at app startup:

```typescript
// In automation-engine-ui/react/src/main.tsx or similar
import "./components/widgets/ColorPickerWidget";
```

### Step 3: Use in Context Class

```java
@PresentationHint(
    widget = PresentationHint.Widget.CUSTOM,
    customComponent = "colorPicker",
    helpText = "Select a background color"
)
private String backgroundColor;
```

### CustomWidgetProps Interface

```typescript
export interface CustomWidgetProps {
    name: string;                          // Field name for form registration
    value: unknown;                        // Current field value
    onChange: (value: unknown) => void;    // Callback to update value
    control: Control<FieldValues>;         // React Hook Form control
    schema: Record<string, unknown>;       // Field JSON Schema
    customProps?: Record<string, unknown>; // Custom properties from annotation
    label: string;                         // Field label
}
```

## Real-World Examples

### LoggerActionContext

```java
@Data
public class LoggerActionContext implements IActionContext {
    private String alias;
    private String description;
    
    @PresentationHint(
        helpText = "Select log level: TRACE (most verbose), DEBUG, INFO, WARN, or ERROR (least verbose)"
    )
    private Level level = Level.INFO;
    
    @PresentationHint(
        widget = PresentationHint.Widget.TEXTAREA,
        placeholder = "Enter log message (supports template expressions like {{ event.data }})",
        helpText = "Message to log. Supports Pebble template expressions for dynamic content."
    )
    private Object message = "No message";
}
```

### SendHttpRequestActionContext

```java
@Data
public class SendHttpRequestActionContext implements IActionContext {
    private String alias;
    private String description;
    
    @PresentationHint(
        placeholder = "https://api.example.com/endpoint",
        helpText = "Full URL including protocol. Supports template expressions like {{ event.userId }}"
    )
    private String url;
    
    @PresentationHint(
        helpText = "HTTP method: GET (retrieve), POST (create), PUT (update), DELETE (remove), PATCH (partial update)"
    )
    private HttpMethod method;
    
    @PresentationHint(
        widget = PresentationHint.Widget.TEXTAREA,
        placeholder = "{\"key\": \"value\"}",
        helpText = "Request payload. Can be JSON, XML, or form data based on Content-Type. Supports template expressions."
    )
    private Object body;
    
    @PresentationHint(
        placeholder = "responseData",
        helpText = "Variable name to store the response for later use in the automation"
    )
    private String storeToVariable;
}
```

## JSON Schema Output

The annotation generates custom properties in the JSON Schema:

```json
{
  "type": "object",
  "properties": {
    "message": {
      "type": "string",
      "x-presentation-widget": "textarea",
      "x-presentation-placeholder": "Enter log message",
      "x-presentation-help": "Message to log. Supports Pebble template expressions.",
      "x-presentation-validation": {
        "required": true,
        "pattern": "^.{1,1000}$"
      }
    }
  }
}
```

## Architecture

### Backend (Java)

1. **@PresentationHint Annotation** (`automation-engine-spring/spi/annotation/`)
   - Defines presentation metadata on context fields
   - Generic and not UI-specific

2. **PresentationHintModule** (`automation-engine-backend-api/json_schema/`)
   - Victools module that processes annotations
   - Adds `x-presentation-*` properties to JSON Schema

3. **JsonSchemaConfig** (`automation-engine-backend-api/json_schema/`)
   - Registers PresentationHintModule with SchemaGenerator
   - Generates schemas served by REST API

### Frontend (React/TypeScript)

1. **CustomWidgetRegistry** (`react/src/lib/`)
   - Registry for custom widget components
   - `Map<string, ComponentType<CustomWidgetProps>>`

2. **FieldRenderer** (`react/src/components/`)
   - Reads `x-presentation-*` properties from schema
   - Renders appropriate shadcn/ui components
   - Falls back to default rendering if no hints present

## Best Practices

1. **Use Annotations Sparingly**: Only annotate fields that need custom presentation
2. **Prefer Standard Widgets**: Use built-in widgets before creating custom ones
3. **Provide Help Text**: Always include helpful descriptions for complex fields
4. **Leverage Jakarta Validation**: Use `@NotNull`, `@Min`, `@Max` for backend validation
5. **Keep Placeholders Short**: Use `helpText` for detailed explanations
6. **Test Custom Widgets**: Ensure custom widgets handle all edge cases

## Migration from Existing Code

Existing context classes continue to work without changes:

```java
// Before - still works perfectly
public class OldContext implements IActionContext {
    private String alias;
    private String description;
    private String message;
}

// After - enhanced UX when desired
public class EnhancedContext implements IActionContext {
    private String alias;
    private String description;
    
    @PresentationHint(
        widget = PresentationHint.Widget.TEXTAREA,
        helpText = "Enhanced with better UI hints"
    )
    private String message;
}
```

## Troubleshooting

### Annotation Not Taking Effect

1. **Check import**: Ensure you're importing from `com.davidrandoll.automation.spi.annotation.PresentationHint`
2. **Rebuild project**: Run `mvn clean install` to regenerate schemas
3. **Clear browser cache**: Schema changes may be cached
4. **Check JSON Schema**: Verify `x-presentation-*` properties in schema endpoint

### Custom Widget Not Found

1. **Register widget**: Call `registerCustomWidget()` before app initialization
2. **Import widget file**: Ensure widget file is imported in `main.tsx`
3. **Check component name**: Verify `customComponent` matches registered name
4. **Console errors**: Check browser console for registration errors

### Dropdown Not Showing Options

1. **Specify options**: Use `dropdownOptions` or ensure field is an enum
2. **Check enum values**: Verify enum has values defined
3. **Review schema**: Confirm `enum` or `x-presentation-dropdown-options` in schema

## Additional Resources

- [Victools JSON Schema Generator](https://github.com/victools/jsonschema-generator)
- [shadcn/ui Components](https://ui.shadcn.com/)
- [React Hook Form](https://react-hook-form.com/)
- [AutomationEngine Context Pattern](/.github/copilot-instructions.md#core-components-flow)
