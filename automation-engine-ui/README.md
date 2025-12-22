# automation-engine-ui

React-based UI for building, managing, and visualizing AutomationEngine automations.

## Overview

This module provides a modern web interface for creating and managing automations without writing YAML/JSON manually.
Built with React, TanStack Router, and Tailwind CSS. Similar to how Swagger UI works, this UI auto-discovers all
automation components and provides an interactive interface.

## Features

- ✅ **Visual Automation Builder** - Drag-and-drop interface for building automations
- ✅ **Automation Playground** - Execute automations and visualize execution traces with React Flow
- ✅ **Schema-Driven UI** - Auto-generates forms from JSON schemas
- ✅ **Live Preview** - See YAML/JSON output in real-time
- ✅ **Component Browser** - Browse all available triggers, conditions, and actions
- ✅ **Validation** - Instant feedback on automation configuration
- ✅ **Hot Reload** - Development mode with instant updates

## Quick Start

### Adding to Your Application

Add the dependency to your Spring Boot application's `pom.xml`:

```xml
<dependency>
    <groupId>com.davidrandoll</groupId>
    <artifactId>automation-engine-ui</artifactId>
    <version>${automation-engine.version}</version>
</dependency>
```

The UI will automatically be available at:

```
http://localhost:8080/automation-engine-ui
```

## Architecture

### Frontend Stack

- **React 18** - UI framework
- **TanStack Router** - File-based routing
- **Tailwind CSS** - Utility-first styling
- **Vitest** - Testing framework
- **TypeScript** - Type safety (optional)

### Backend Integration

The UI communicates with `automation-engine-backend-api` for:

- Fetching available components (triggers, conditions, actions)
- Retrieving JSON schemas for validation
- Managing automation definitions

## Configuration

You can configure the UI path and enable/disable it using application properties:

```yaml
automation-engine:
  ui:
    enabled: true # Enable or disable the UI (default: true)
    path: /automation-engine-ui # The path where the UI is served (default: /automation-engine-ui)
```

Or in `application.properties`:

```properties
automation-engine.ui.enabled=true
automation-engine.ui.path=/automation-engine-ui
```

### Custom Path Example

To serve the UI at `/my-custom-path`:

```yaml
automation-engine:
  ui:
    path: /my-custom-path
```

Then access it at: `http://localhost:8080/my-custom-path`

### Disabling the UI

To disable the UI (e.g., in production):

```yaml
automation-engine:
  ui:
    enabled: false
```

## Development

### Local React Development

```bash
cd automation-engine-ui/react
npm install
npm run start
```

UI will be available at: http://localhost:3000

### Full Stack Development

Run backend and frontend together:

```bash
# Terminal 1 - Spring Boot backend
cd automation-engine-ui
mvn spring-boot:run

# Terminal 2 - React frontend with proxy
cd automation-engine-ui/react
npm run start
```

Configure proxy in `vite.config.ts`:

```typescript
export default defineConfig({
  server: {
    proxy: {
      "/automation-engine": "http://localhost:8080",
    },
  },
});
```

## UI Components

### Automation Builder

Main interface for creating automations:

1. **Trigger Panel** - Select and configure triggers
2. **Condition Panel** - Add conditional logic
3. **Action Panel** - Define actions to execute
4. **Variable Panel** - Manage automation variables
5. **Preview Panel** - View generated YAML/JSON

### Component Browser

Browse and search all available components:

```tsx
import { useComponentSearch } from "./hooks/useComponentSearch";

function ComponentBrowser() {
  const { components, loading } = useComponentSearch("actions");

  return (
    <div>
      {components.map((component) => (
        <ComponentCard key={component.name} {...component} />
      ))}
    </div>
  );
}
```

### Schema Form Generator

Auto-generate forms from JSON schemas:

```tsx
import { SchemaForm } from "./components/SchemaForm";

function ActionConfig({ actionName }) {
  const schema = useActionSchema(actionName);

  return <SchemaForm schema={schema} onSubmit={(values) => addAction(values)} />;
}
```

## API Integration

### Fetching Schemas

```typescript
// Get all actions with schemas
async function fetchActions(): Promise<Action[]> {
  const response = await fetch("/automation-engine/block/actions?includeSchema=true");
  const data = await response.json();
  return data.blocks;
}

// Get specific action schema
async function fetchActionSchema(name: string): Promise<JSONSchema> {
  const response = await fetch(`/automation-engine/block/${name}/schema`);
  const data = await response.json();
  return data.schema;
}
```

### Generating YAML

```typescript
interface AutomationConfig {
  alias: string;
  description?: string;
  triggers: Trigger[];
  conditions?: Condition[];
  actions: Action[];
}

function generateYAML(config: AutomationConfig): string {
  return YAML.stringify(config);
}
```

## Routes

File-based routing structure:

```
react/src/routes/
├── __root.tsx          # Root layout
├── index.tsx           # Home page
├── automations/
│   ├── index.tsx       # List automations
│   ├── new.tsx         # Create automation
│   └── $id.tsx         # Edit automation
└── components/
    └── index.tsx       # Browse components
```

### Adding Routes

Create new file in `src/routes/`:

```tsx
// src/routes/automations/templates.tsx
import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/automations/templates")({
  component: TemplatesPage,
});

function TemplatesPage() {
  return <div>Automation Templates</div>;
}
```

## Testing

### Run Tests

```bash
cd react
npm run test
```

### Example Test

```tsx
import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { AutomationCard } from "./AutomationCard";

describe("AutomationCard", () => {
  it("displays automation details", () => {
    const automation = {
      alias: "test-automation",
      description: "Test description",
      triggers: [{ trigger: "alwaysTrue" }],
      actions: [{ action: "logger", message: "test" }],
    };

    render(<AutomationCard automation={automation} />);

    expect(screen.getByText("test-automation")).toBeInTheDocument();
  });
});
```

## Build Process

### Maven Build

```bash
mvn clean install
```

This will:

1. Install Node.js and npm
2. Install npm dependencies
3. Build the React application
4. Copy the built files to `src/main/resources/static`
5. Package everything into the Spring Boot JAR

### Frontend Only Build

```bash
cd react
npm run build
```

## How It Works

The module:

1. Automatically builds the React application during Maven build
2. Packages the static files into the JAR under `META-INF/automation-engine-ui/`
3. Configures Spring MVC to serve these static files at the configured path
4. The UI calls the endpoints provided by `automation-engine-backend-api` module

## Requirements

- Java 21+
- Spring Boot 3.x
- Node.js 18+ (for development)
- The `automation-engine-backend-api` module (included as transitive dependency)

## Dependencies

### Frontend

- `react` - UI framework
- `@tanstack/react-router` - Routing
- `tailwindcss` - Styling
- `js-yaml` - YAML parsing

### Backend

- `automation-engine-backend-api` - REST API
- `org.springframework.boot:spring-boot-starter-web` - Web server

## See Also

- **[automation-engine-backend-api](../automation-engine-backend-api/README.md)** - REST API documentation
- **[TanStack Router](https://tanstack.com/router)** - Routing documentation
- **[Tailwind CSS](https://tailwindcss.com/)** - Styling documentation
- **[React Documentation](https://react.dev/)** - React framework

### Local Development of the React App

Navigate to the `react` directory and run:

```bash
cd react
npm install
npm run dev
```

This will start a development server with hot-reloading.
