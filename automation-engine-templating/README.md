# automation-engine-templating

Provides Pebble and SpEL template engine integration for dynamic expression evaluation in AutomationEngine.

## Overview

This module integrates the [Pebble template engine](https://pebbletemplates.io/) and
[Spring Expression Language (SpEL)](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#expressions)
to enable dynamic value resolution throughout automations.

- **Pebble** uses the `{{ expression }}` syntax.
- **SpEL** uses the `#{expression}` syntax.

Template expressions can access event properties, variables, and metadata.

## Key Features

- ✅ **Multi-Engine Support** - Support for both Pebble and SpEL
- ✅ **Expression Evaluation** - Process `{{ }}` (Pebble) or `#{ }` (SpEL) template expressions
- ✅ **Event Access** - Access event properties: `{{ event.user.email }}` or `#{event.user.email}`
- ✅ **Variable Access** - Use automation variables: `{{ myVariable }}` or `#{myVariable}`
- ✅ **Metadata Access** - Access stored values: `{{ storedResult.id }}` or `#{storedResult.id}`
- ✅ **Conditional Logic** - Support for `if/else`, loops, filters (Pebble) and complex Java expressions (SpEL)
- ✅ **Custom Functions** - Extensible with custom Pebble functions
- ✅ **Type Safety** - Automatic type conversion and null handling

## Components

### TemplateProcessor

`com.davidrandoll.automation.engine.templating.TemplateProcessor`

Main class for processing templates. It delegates to the appropriate engine based on the provided `templatingType`.

**Usage:**

```java
@Autowired
private TemplateProcessor templateProcessor;

public void processTemplate() {
    Map<String, Object> context = Map.of(
        "user", new User("John", "john@example.com"),
        "count", 5
    );

    // Using Pebble (default)
    String pebbleResult = templateProcessor.process(
        "Hello {{ user.name }}, you have {{ count }} messages",
        context
    );
    // Result: "Hello John, you have 5 messages"

    // Using SpEL
    String spelResult = templateProcessor.process(
        "Hello #{user.name}, you have #{count} messages",
        context,
        "spel"
    );
    // Result: "Hello John, you have 5 messages"
}
```

**Key Methods:**

```java
public class TemplateProcessor {
    // Process template with context
    String process(String template, Map<String, Object> context);

    // Process template without context
    String process(String template);

    // Get underlying Pebble engine
    PebbleEngine getPebbleEngine();
}
```

## Template Syntax

### Basic Expressions

```yaml
# Access event properties
message: "User {{ event.username }} logged in"

# Nested properties
email: "{{ event.user.profile.email }}"

# Array/List access
firstItem: "{{ event.items[0] }}"

# Map access
value: "{{ event.metadata['key'] }}"
```

### Conditionals

```yaml
# If/else
message: "{% if event.priority == 'HIGH' %}Urgent{% else %}Normal{% endif %}"

# Ternary
status: "{{ event.active ? 'Active' : 'Inactive' }}"
```

### Filters

Pebble provides many built-in filters. AutomationEngine extends these with custom filters:

#### Custom Filters

**Data Conversion:**

- `json` - Convert objects to JSON: `{{ event.user | json }}`
- `fromJson` - Parse JSON strings: `{{ jsonString | fromJson.name }}`
- `int` - Convert to integer: `{{ value | int }}`

**Date/Time Formatting:**

- `date_format` - Format dates: `{{ event.createdAt | date_format('yyyy-MM-dd HH:mm:ss') }}`
- `time_format` - Format times: `{{ event.time | time_format('HH:mm a') }}`
- `number_format` - Format numbers: `{{ value | number_format(2) }}`

**Encoding/Decoding:**

- `base64encode` - Encode to Base64: `{{ credentials | base64encode }}`
- `base64decode` - Decode from Base64: `{{ encoded | base64decode }}`
- `urlEncode` - URL encode: `{{ searchTerm | urlEncode }}`
- `urlDecode` - URL decode: `{{ encoded | urlDecode }}`

**Utility:**

- `coalesce` - First non-null value: `{{ event.assignee | coalesce('unassigned') }}`

See [FILTERS.md](FILTERS.md) for detailed documentation and examples.

#### Standard Pebble Filters

```yaml
# String filters
uppercase: "{{ event.name | upper }}"
lowercase: "{{ event.email | lower }}"
capitalize: "{{ event.title | capitalize }}"

# Number filters
formatted: "{{ event.amount | numberformat('#,##0.00') }}"

# Default values
value: "{{ event.optional | default('N/A') }}"
```

### Loops

```yaml
# In YAML, use multi-line strings
message: |
  {% for item in event.items %}
  - {{ item.name }}: {{ item.price }}
  {% endfor %}
```

### Mathematical Operations

```yaml
total: "{{ event.price * event.quantity }}"
average: "{{ (event.total / event.count) | round }}"
```

## Usage in Automations

### In Triggers

```yaml
triggers:
  - trigger: template
    expression: "{{ event.amount > 1000 }}"
```

### In Conditions

```yaml
conditions:
  - condition: template
    expression: "{{ event.status == 'PENDING' and event.priority == 'HIGH' }}"
```

### In Actions

```yaml
actions:
  - action: sendEmail
    to: "{{ event.user.email }}"
    subject: "Order #{{ event.orderId }} - {{ event.status }}"
    body: |
      Hello {{ event.user.name }},

      Your order #{{ event.orderId }} is now {{ event.status }}.
      Total: ${{ event.total | numberformat('#,##0.00') }}

      Items:
      {% for item in event.items %}
      - {{ item.name }} ({{ item.quantity }}x): ${{ item.price }}
      {% endfor %}
```

### In Variables

```yaml
variables:
  fullName: "{{ event.firstName }} {{ event.lastName }}"
  discountedPrice: "{{ event.price * 0.9 }}"
  formattedDate: "{{ event.timestamp | date('yyyy-MM-dd') }}"
```

### In Results

```yaml
result:
  id: "{{ todo.id }}"
  title: "{{ todo.title }}"
  assignee: "{{ todo.assignee.username }}"
  createdAt: "{{ todo.createdAt | date('ISO') }}"
```

## Advanced Features

### Accessing EventContext

In actions and conditions, you can access the full `EventContext`:

```java
@Component("customAction")
public class CustomAction extends PluggableAction<CustomActionContext> {

    @Override
    public void doExecute(EventContext ec, CustomActionContext ac) {
        // Resolve expression with event context
        String result = resolveExpression(ec, "{{ event.user.name }}");

        // Or use TemplateProcessor directly
        Map<String, Object> context = new HashMap<>();
        context.put("event", ec.getEvent());
        context.put("metadata", ec.getMetadata());

        String processed = templateProcessor.process(ac.getTemplate(), context);
    }
}
```

### Custom Functions

Extend Pebble with custom functions:

```java
@Component
public class CustomPebbleExtension extends AbstractExtension {

    @Override
    public Map<String, Function> getFunctions() {
        return Map.of("formatCurrency", new FormatCurrencyFunction());
    }

    private static class FormatCurrencyFunction implements Function {
        @Override
        public Object execute(Map<String, Object> args, PebbleTemplate self,
                            EvaluationContext context, int lineNumber) {
            Double amount = (Double) args.get("amount");
            return String.format("$%.2f", amount);
        }

        @Override
        public List<String> getArgumentNames() {
            return List.of("amount");
        }
    }
}
```

**Usage:**

```yaml
actions:
  - action: logger
    message: "Total: {{ formatCurrency(amount: event.total) }}"
```

## Null Safety

Pebble handles null values gracefully:

```yaml
# Returns empty string if user or email is null
email: "{{ event.user.email }}"

# Provide default value
email: "{{ event.user.email | default('no-email@example.com') }}"

# Check for null
message: "{% if event.user %}Hello {{ event.user.name }}{% endif %}"
```

## Type Conversion

The template engine automatically converts types:

```yaml
# Numbers to strings
count: "You have {{ event.count }} items"  # Works even if count is int

# Booleans to strings
status: "Active: {{ event.isActive }}"     # "Active: true"

# Dates to strings
date: "{{ event.timestamp }}"              # Uses toString()
date: "{{ event.timestamp | date('yyyy-MM-dd') }}"  # Formatted
```

## Error Handling

Template processing errors are wrapped in `RuntimeException`:

```java
try {
    String result = templateProcessor.process("{{ invalid..syntax }}", context);
} catch (RuntimeException e) {
    log.error("Template processing failed", e);
}
```

## Performance Considerations

- Templates are compiled and cached by Pebble
- Reusing the same template string is efficient
- Complex templates with many loops may impact performance
- Consider pre-computing values in variables if the same expression is used multiple times

## Testing

```java
@SpringBootTest
class TemplateProcessorTest {

    @Autowired
    private TemplateProcessor processor;

    @Test
    void testSimpleExpression() {
        Map<String, Object> context = Map.of("name", "John");
        String result = processor.process("Hello {{ name }}", context);
        assertEquals("Hello John", result);
    }

    @Test
    void testComplexExpression() {
        User user = new User("John", "john@example.com");
        Map<String, Object> context = Map.of("user", user);

        String result = processor.process(
            "{{ user.name | upper }} - {{ user.email }}",
            context
        );
        assertEquals("JOHN - john@example.com", result);
    }
}
```

## Dependencies

- `io.pebbletemplates:pebble` - Pebble template engine
- `automation-engine-core` - Core automation interfaces

## See Also

- **[Pebble Documentation](https://pebbletemplates.io/wiki/guide/basic-usage/)** - Official Pebble guide
- **[automation-engine-spring](../automation-engine-spring/README.md)** - Spring integration
- **[AI_PROMPT.md](../AI_PROMPT.md)** - Template syntax examples in automation context
