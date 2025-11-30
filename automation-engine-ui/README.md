# Automation Engine UI Module

This module provides a React-based user interface for the Automation Engine, similar to how Swagger UI works.

## Usage

### Adding to Your Application

Simply add the dependency to your Spring Boot application's `pom.xml`:

```xml
<dependency>
    <groupId>com.davidrandoll</groupId>
    <artifactId>automation-engine-ui</artifactId>
    <version>${automation-engine.version}</version>
</dependency>
```

When you run your application, the UI will automatically be available at:
```
http://localhost:{your-port}/automation-engine-ui
```

### Configuration

You can configure the UI path and enable/disable it using application properties:

```yaml
automation-engine:
  ui:
    enabled: true  # Enable or disable the UI (default: true)
    path: /automation-engine-ui  # The path where the UI is served (default: /automation-engine-ui)
```

Or in `application.properties`:

```properties
automation-engine.ui.enabled=true
automation-engine.ui.path=/automation-engine-ui
```

### Example

For example, to serve the UI at `/my-custom-path`:

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

## How It Works

The module:
1. Automatically builds the React application during Maven build
2. Packages the static files into the JAR under `META-INF/automation-engine-ui/`
3. Configures Spring MVC to serve these static files at the configured path
4. The UI calls the endpoints provided by `automation-engine-backend-api` module

## Requirements

- Java 21+
- Spring Boot 3.x
- The `automation-engine-backend-api` module must be included (it's automatically added as a transitive dependency)

## Development

### Building the Module

```bash
mvn clean install
```

This will:
1. Install Node.js and npm
2. Install npm dependencies
3. Build the React application
4. Copy the built files to the resources directory
5. Package everything into the JAR

### Local Development of the React App

Navigate to the `react` directory and run:

```bash
cd react
npm install
npm run dev
```

This will start a development server with hot-reloading.

