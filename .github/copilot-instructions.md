# AutomationEngine - AI Coding Agent Guide

## Project Overview

AutomationEngine is a **multi-module Maven project** that provides an event-driven automation framework for Java/Spring
Boot applications. Automations are defined via YAML/JSON configs or code, responding to triggers with conditional
actions.

**Core Philosophy**: Pluggable architecture where triggers, conditions, actions, variables, and results are all
extensible through Spring components.

## Architecture & Module Structure

### Module Hierarchy (11 modules)

```
automation-engine (parent pom)
├── automation-engine-core             # Core automation logic (no Spring)
├── automation-engine-templating       # Pebble template engine integration
├── automation-engine-spring           # Spring base layer & SPI (PluggableAction, PluggableCondition, etc.)
├── automation-engine-spring-web       # HTTP triggers/actions (onHttpRequest, sendHttpRequest)
├── automation-engine-spring-events    # Spring ApplicationEvent integration
├── automation-engine-spring-jdbc      # Database triggers/actions (onJdbcQuery, jdbcExecute)
├── automation-engine-spring-amqp      # RabbitMQ support (publishRabbitMqEvent)
├── automation-engine-test             # Test utilities (EventCaptureListener)
├── automation-engine-ui               # React UI + Spring backend
├── automation-engine-backend-api      # REST API for automation management
├── spring-boot-starter-automation-engine  # Starter that aggregates all modules
└── automation-engine-test-coverage-report # JaCoCo aggregate coverage
```

**Key Insight**: `automation-engine-core` is Spring-agnostic. All Spring integration happens in
`automation-engine-spring` and its submodules.

### Core Components Flow

1. **AutomationEngine** (`automation-engine-core`) - Main API facade

   - Methods: `register()`, `publishEvent()`, `executeAutomationWithYaml()`
   - Delegates to `IAEOrchestrator` for execution

2. **Automation** (`automation-engine-core/core/Automation.java`) - Immutable automation definition

   - Contains: alias, variables, triggers, conditions, actions, result
   - Execution flow: `resolveVariables()` → `anyTriggerActivated()` → `allConditionsMet()` → `performActions()` →
     `getExecutionSummary()`

3. **AutomationOrchestrator** (`automation-engine-core/orchestrator`) - Manages registered automations

   - Maintains `CopyOnWriteArrayList<Automation>` for thread-safe registration
   - Interceptor chain pattern for extensibility (`IAutomationExecutionInterceptor`,
     `IAutomationHandleEventInterceptor`)

4. **AutomationFactory/AutomationCreator** - Parses YAML/JSON/programmatic definitions
   - Uses `IAutomationFormatParser<T>` implementations (YamlParser, JsonParser, ManualAutomationBuilder)

## Extension Pattern: The "Pluggable" SPI

### Creating Custom Actions (most common extension)

**Pattern**: Extend `PluggableAction<T>` where T is your context class

```java
@Component("myActionName")  // Bean name = YAML action identifier
@Slf4j
public class MyCustomAction extends PluggableAction<MyCustomActionContext> {
    @Override
    public void doExecute(EventContext ec, MyCustomActionContext ac) {
        // Implementation - access Spring beans via @Autowired fields
        log.info("Executing with param: {}", ac.getMyParam());
    }
}

@Data  // Always use Lombok @Data for context classes
public class MyCustomActionContext implements IActionContext {
    private String alias;
    private String description;
    private String myParam;  // Auto-mapped from YAML properties
}
```

**Why this pattern?**

- `PluggableAction` provides dependency injection for `AutomationProcessor`, `ITypeConverter`, and `ApplicationContext`
- Uses `@Delegate` to expose `AutomationProcessor` methods (e.g., `resolveExpression()`)
- `getSelf()` returns proxied bean to ensure AOP aspects (transactions, logging) apply

**Usage in YAML**:

```yaml
actions:
  - action: myActionName # Must match @Component name
    myParam: "value"
```

### Creating Custom Conditions

Same pattern as actions, extend `PluggableCondition<T>`:

```java
@Component("isBusinessHours")
public class IsBusinessHoursCondition extends PluggableCondition<EmptyConditionContext> {
    @Override
    public boolean evaluate(EventContext ec, EmptyConditionContext ctx) {
        LocalTime now = LocalTime.now();
        return !now.isBefore(LocalTime.of(9, 0)) && !now.isAfter(LocalTime.of(17, 0));
    }
}
```

### Creating Custom Triggers

Extend `PluggableTrigger<T>`:

```java
@Component("myTrigger")
public class MyTrigger extends PluggableTrigger<MyTriggerContext> {
    @Override
    public boolean isActivated(EventContext ec, MyTriggerContext tc) {
        return /* condition logic */;
    }
}
```

## Testing Conventions

### Test Coverage Requirements

- **Minimum coverage: 80%** (enforced by JaCoCo in parent pom.xml line 194)
- Build fails if coverage drops below threshold
- Run coverage check: `mvn verify`
- Aggregate report: `mvn verify -pl automation-engine-test-coverage-report`

### Test File Location

- Mirror source structure: `src/test/java/com/davidrandoll/...`
- Use `@SpringBootTest` for integration tests
- Use `@ExtendWith(MockitoExtension.class)` for unit tests

### Common Test Patterns

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional  // Rollback after each test
class MyIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private AutomationEngine engine;

    @Test
    void testAutomationExecution() {
        // Pattern: Create YAML → Execute → Assert result
        String yaml = """
            alias: test-automation
            triggers: [...]
            actions: [...]
            """;
        var result = engine.executeAutomationWithYaml(yaml, event);
        assertThat(result.isExecuted()).isTrue();
    }
}
```

## Build & Development Workflow

### Essential Maven Commands

```bash
# Build all modules (skip tests)
mvn clean install -DskipTests

# Build with tests and coverage
mvn clean verify

# Build specific module and dependencies
mvn clean install -pl automation-engine-core -am

# Run coverage report
mvn verify -pl automation-engine-test-coverage-report

# Flatten POMs for release (uses ${revision} variable)
mvn flatten:flatten
```

### Multi-Module Dependencies

- **Always** build parent first or use `-am` (also-make) flag
- Version management: Uses `${revision}` property (currently 1.7.0)
- Flattened POMs generated for Maven Central compatibility

### Java Version

- **Java 21** required (see `maven.compiler.source/target` in pom.xml)
- Uses features like text blocks (`"""..."""`) extensively in tests

## Project-Specific Patterns

### Pebble Templating Everywhere

- All dynamic values use `{{ expression }}` syntax
- Available in: triggers, conditions, actions, results, variables
- Example: `"{{ event.user.email }}"` accesses nested event properties

### User-Defined Features

- Support for reusable parameterized components (see `USER_DEFINED_FEATURES.md`)
- Registries: `IUserDefinedActionRegistry`, `IUserDefinedConditionRegistry`, `IUserDefinedTriggerRegistry`
- Pattern: Define once, use with parameters across automations

### Event-Driven Architecture

- Events implement `IEvent` marker interface
- `EventContext` wraps event with variables map for execution context
- `AutomationOrchestrator` publishes lifecycle events (`AutomationEngineRegisterEvent`,
  `AutomationEngineProcessedEvent`)

### Immutability & Thread Safety

- `Automation` class is immutable (final fields, copy constructor)
- `CopyOnWriteArrayList` for automation registry
- EventContext allows variable mutation during execution

## Common Pitfalls & Solutions

1. **Action not found error**

   - Ensure `@Component("name")` matches YAML `action: name`
   - Check Spring component scan includes your package

2. **Type conversion failures**

   - Context class must implement the appropriate interface (IActionContext, IConditionContext, ITriggerContext,
     IVariableContext, IResultContext)
   - Context class must have matching field names (case-sensitive)
   - Always include `alias` and `description` fields (required by context interfaces)
   - Use `@Data` on context classes for getters/setters

3. **AOP aspects not applying**

   - Use `getSelf()` in `PluggableAction` to get proxied bean
   - Example: `getSelf().doExecute(...)` instead of `this.doExecute(...)`

4. **Coverage failures**
   - Test module separately first: `mvn test -pl <module>`
   - Exclude generated code with JaCoCo config if needed

5. **Using alias as an identifier**
   - **NEVER** use the automation `alias` as a unique identifier. It is not guaranteed to be unique.
   - Always use the automation `id` (UUID) for tracking, state management, or identification.

## Additional Resources

- **AI_PROMPT.md**: Comprehensive YAML/JSON automation syntax guide
- **USER_DEFINED_FEATURES.md**: User-defined action/condition/trigger patterns
- **examples/todo-example**: Full working example with custom actions
- **README.md**: Getting started guide with basic usage
