# Field Descriptions for Context Types - Implementation Guide

## Overview
This document describes how to add field descriptions to AutomationEngine context types so they appear in generated JSON schemas.

## Solution

We've implemented a custom schema description system that uses the `@SchemaDescription` annotation alongside Javadoc comments.

### Components Added

1. **@SchemaDescription Annotation** (`automation-engine-backend-api/json_schema/SchemaDescription.java`)
   - Runtime-retained annotation for fields and types
   - Provides description text for JSON schema generation

2. **JavadocDescriptionModule** (`automation-engine-backend-api/json_schema/JavadocDescriptionModule.java`)
   - Custom Victools module that extracts descriptions from `@SchemaDescription` annotations
   - Integrated into `JsonSchemaConfig`

3. **Updated JsonSchemaConfig**
   - Now includes `JavadocDescriptionModule` in the schema generator configuration

## Usage Pattern

For each context class (Actions, Triggers, Conditions, Variables, Results), add both:

1. **Javadoc comments** (for developer documentation)
2. **@SchemaDescription annotations** (for runtime schema generation)

### Example

```java
import com.davidrandoll.automation.engine.backend.api.json_schema.SchemaDescription;

public class MyActionContext implements IActionContext {
    /** Unique identifier for this action */
    @SchemaDescription("Unique identifier for this action")
    private String alias;
    
    /** Human-readable description of what this action does */
    @SchemaDescription("Human-readable description of what this action does")
    private String description;
    
    /** The URL to send the HTTP request to */
    @SchemaDescription("The URL to send the HTTP request to")
    private String url;
}
```

## Context Classes Updated

The following context classes have been updated with Javadoc comments (and sample @SchemaDescription annotations):

### JDBC Module
- ✅ `JdbcQueryActionContext` (with @SchemaDescription)
- ✅ `JdbcExecuteActionContext`
- ✅ `OnJdbcQueryTriggerContext`

### Spring Web Module
- ✅ `OnHttpRequestTriggerContext`
- ✅ `OnHttpResponseTriggerContext`
- ✅ `SendHttpRequestActionContext`
- ✅ `MatchContext` (base class for conditions)
- ✅ `HttpMethodConditionContext`
- ✅ `HttpHeaderConditionContext`
- ✅ `HttpQueryParamConditionContext`

### Spring Module (Core)
- ✅ `LoggerActionContext`
- ✅ `DelayActionContext`
- ✅ `VariableActionContext`
- ✅ `BasicVariableContext`
- ✅ `BasicResultContext`
- ✅ `TimeBasedTriggerContext`
- ✅ `IfThenElseActionContext` (including nested classes)
- ✅ `RepeatActionContext`
- ✅ `SequenceActionContext`
- ✅ `ParallelActionContext`
- ✅ `OnEventTypeTriggerContext`

### Spring AMQP Module
- ✅ `PublishRabbitMqEventActionContext` (including nested Exchange and Queue classes)

### Spring Events Module
- ✅ `PublishSpringEventActionContext`

## Next Steps

To complete the implementation across all context types:

1. **Add @SchemaDescription to remaining context classes** - systematically go through all 50+ context classes
2. **Verify schema generation** - Run the test to ensure descriptions appear in schemas
3. **Update documentation** - Add examples to README showing how field descriptions appear in UIs

## Testing

Run the test to verify schema generation includes descriptions:

```bash
mvn test -pl automation-engine-backend-api -Dtest=SchemaDescriptionTest
```

## Benefits

1. **Better IDE support** - Javadoc provides inline documentation for developers
2. **Rich UI schemas** - @SchemaDescription enables UI builders to show helpful hints
3. **Maintainable** - Single source of truth for field documentation
4. **No runtime overhead** - Descriptions cached during schema generation

