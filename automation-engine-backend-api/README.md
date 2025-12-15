# automation-engine-backend-api

REST API for managing AutomationEngine automations and schemas.

## Overview

This module provides a RESTful API for querying automation components, retrieving JSON schemas, and managing
automations. It's designed to support UI builders and external tools that need to interact with AutomationEngine
programmatically.

## Features

- ✅ **Schema Discovery** - Retrieve JSON schemas for all automation components
- ✅ **Block Metadata** - Query available triggers, conditions, actions, variables, and results
- ✅ **JSON Schema Generation** - Auto-generate schemas for IDE autocomplete and validation
- ✅ **User-Defined Components** - API endpoints for custom user-defined features
- ✅ **Dynamic Discovery** - Automatically detects all registered Spring components

## API Endpoints

### Get All Block Schemas

Retrieve all available automation blocks with their JSON schemas.

```http
GET /automation-engine/blocks/schemas
```

**Response:**

```json
{
  "triggers": [
    {
      "name": "onHttpRequest",
      "schema": { "$schema": "...", "properties": {...} }
    }
  ],
  "conditions": [...],
  "actions": [...],
  "variables": [...],
  "results": [...]
}
```

### Get Blocks by Type

Retrieve all blocks of a specific type (triggers, conditions, actions, variables, results).

```http
GET /automation-engine/block/{type}?includeSchema=true
```

**Parameters:**

- `type` (path) - One of: `triggers`, `conditions`, `actions`, `variables`, `results`
- `includeSchema` (query, optional) - Include JSON schema in response (default: false)

**Example:**

```http
GET /automation-engine/block/actions?includeSchema=true
```

**Response:**

```json
{
  "type": "actions",
  "blocks": [
    {
      "name": "logger",
      "schema": {
        "$schema": "http://json-schema.org/draft-07/schema#",
        "type": "object",
        "properties": {
          "message": { "type": "string" }
        }
      }
    }
  ]
}
```

### Get Schema by Block Name

Retrieve JSON schema for a specific block.

```http
GET /automation-engine/block/{name}/schema
```

**Example:**

```http
GET /automation-engine/block/sendHttpRequest/schema
```

**Response:**

```json
{
  "name": "sendHttpRequest",
  "schema": {
    "$schema": "http://json-schema.org/draft-07/schema#",
    "type": "object",
    "properties": {
      "url": { "type": "string" },
      "method": { "enum": ["GET", "POST", "PUT", "DELETE"] }
    }
  }
}
```

### Get Automation Definition Schema

Get the complete JSON schema for automation definition structure.

```http
GET /automation-engine/automation-definition/schema
```

**Response:**

```json
{
  "name": "AutomationDefinition",
  "schema": {
    "$schema": "http://json-schema.org/draft-07/schema#",
    "type": "object",
    "properties": {
      "alias": { "type": "string" },
      "description": { "type": "string" },
      "triggers": { "type": "array" },
      "conditions": { "type": "array" },
      "actions": { "type": "array" }
    },
    "required": ["alias"]
  }
}
```

### Get Full Automation Schema

Download complete JSON schema for all automation components. Use this for IDE autocomplete and validation.

```http
GET /automation-engine/schema.json
```

**Response:**

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "$id": "http://localhost:8080/automation-engine/schema.json",
  "definitions": {
    "AutomationDefinition": {...},
    "triggers": {...},
    "conditions": {...},
    "actions": {...}
  }
}
```

## Integration Examples

### IDE Autocomplete Setup

**VS Code - settings.json:**

```json
{
  "yaml.schemas": {
    "http://localhost:8080/automation-engine/schema.json": ["*.automation.yaml", "*.automation.yml"]
  }
}
```

**IntelliJ IDEA:**

1. Go to Settings → Languages & Frameworks → Schemas and DTDs → JSON Schema Mappings
2. Add new mapping:
   - Name: `AutomationEngine`
   - Schema URL: `http://localhost:8080/automation-engine/schema.json`
   - Schema version: `JSON Schema version 7`
   - File path pattern: `*.automation.yaml`

### Query Available Actions

```javascript
// Fetch all available actions
fetch("http://localhost:8080/automation-engine/block/actions?includeSchema=true")
  .then((res) => res.json())
  .then((data) => {
    console.log(
      "Available actions:",
      data.blocks.map((b) => b.name)
    );
  });
```

### Build UI Form from Schema

```typescript
interface BlockSchema {
  name: string;
  schema: {
    properties: Record<string, any>;
  };
}

async function loadActionSchema(actionName: string): Promise<BlockSchema> {
  const response = await fetch(`http://localhost:8080/automation-engine/block/${actionName}/schema`);
  return response.json();
}

// Use schema to dynamically generate form fields
const schema = await loadActionSchema("sendHttpRequest");
schema.schema.properties.forEach((prop, name) => {
  // Generate UI input based on property type
  if (prop.type === "string") {
    createTextInput(name);
  } else if (prop.enum) {
    createDropdown(name, prop.enum);
  }
});
```

### Validate Automation Before Execution

```java
@Service
@RequiredArgsConstructor
public class AutomationValidationService {
    private final IAESchemaService schemaService;
    private final ObjectMapper objectMapper;

    public boolean validateAutomation(String yaml) throws Exception {
        JsonNode schema = schemaService.getFullAutomationSchema("http://localhost:8080");
        JsonNode automation = objectMapper.readTree(yaml);

        JsonSchema jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
            .getSchema(schema);

        Set<ValidationMessage> errors = jsonSchema.validate(automation);
        return errors.isEmpty();
    }
}
```

## User-Defined Components API

### Get User-Defined Actions

```http
GET /automation-engine/user-defined/actions
```

### Get User-Defined Conditions

```http
GET /automation-engine/user-defined/conditions
```

### Get User-Defined Triggers

```http
GET /automation-engine/user-defined/triggers
```

**Response Example:**

```json
[
  {
    "name": "myCustomAction",
    "parameters": [
      { "name": "param1", "type": "string" },
      { "name": "param2", "type": "integer" }
    ]
  }
]
```

## Configuration

Enable the backend API in your Spring Boot application:

```java
@SpringBootApplication
@Import(AEBackendApiAutoConfiguration.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

Or use the Spring Boot starter which includes it:

```xml
<dependency>
    <groupId>com.davidrandoll</groupId>
    <artifactId>spring-boot-starter-automation-engine</artifactId>
    <version>${revision}</version>
</dependency>
```

## CORS Configuration

For external UIs, configure CORS:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/automation-engine/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
```

## Dependencies

- `automation-engine-spring` - Spring integration
- `com.github.victools:jsonschema-generator` - JSON schema generation
- `org.springframework.boot:spring-boot-starter-web` - REST API

## See Also

- **[automation-engine-ui](../automation-engine-ui/README.md)** - React UI using this API
- **[JSON Schema Specification](https://json-schema.org/)** - JSON Schema docs
