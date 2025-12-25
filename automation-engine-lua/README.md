# Automation Engine Lua

Provides Lua scripting capabilities for the Automation Engine, enabling custom logic through Lua scripts in triggers,
conditions, actions, variables, and results.

## Features

- **LuaScript Action**: Execute Lua scripts as part of automation actions
- **LuaScript Trigger**: Trigger automations based on Lua script evaluation
- **LuaScript Condition**: Evaluate conditions using Lua scripts
- **LuaScript Variable**: Compute variables using Lua scripts
- **LuaScript Result**: Generate results using Lua scripts

## Usage

### Action Example

Execute a Lua script as an action:

```yaml
alias: lua-action-example
triggers:
  - trigger: alwaysTrue
actions:
  - action: luaScript
    script: |
      -- Access event data
      local username = event.username
      log.info("Processing user: " .. username)
      
      -- Set variables for subsequent actions
      result.processedName = string.upper(username)
      result.timestamp = os.time()
```

### Trigger Example

Trigger automation based on Lua script evaluation:

```yaml
alias: lua-trigger-example
triggers:
  - trigger: luaScript
    script: |
      -- Return true to trigger, false otherwise
      return event.priority == "HIGH" and event.count > 10
actions:
  - action: logger
    message: "High priority event detected!"
```

### Condition Example

Use Lua script as a condition:

```yaml
alias: lua-condition-example
triggers:
  - trigger: alwaysTrue
conditions:
  - condition: luaScript
    script: |
      -- Return true if condition is met
      local hour = os.date("*t").hour
      return hour >= 9 and hour < 17  -- Business hours
actions:
  - action: logger
    message: "Action executed during business hours"
```

### Variable Example

Compute variables using Lua:

```yaml
alias: lua-variable-example
variables:
  - variable: luaScript
    script: |
      -- Return a table with variable values
      return {
        greeting = "Hello, " .. event.name,
        uppercaseName = string.upper(event.name),
        timestamp = os.time()
      }
triggers:
  - trigger: alwaysTrue
actions:
  - action: logger
    message: "{{ greeting }}"
```

### Result Example

Generate dynamic results using Lua:

```yaml
alias: lua-result-example
triggers:
  - trigger: alwaysTrue
actions:
  - action: logger
    message: "Processing..."
result:
  result: luaScript
  script: |
    -- Return the automation result
    return {
      status = "completed",
      processedAt = os.date(),
      eventName = event.name
    }
```

## Available Bindings

The following objects are available in Lua scripts:

| Binding | Description |
|---------|-------------|
| `event` | The event data as a Lua table |
| `metadata` | Additional metadata from the EventContext |
| `log` | Logger object with `info()`, `warn()`, `error()`, `debug()` methods |
| `result` | (Actions only) Table to store results for subsequent actions |
| `json` | JSON utility with `encode(table)` and `decode(string)` methods |

## Dependencies

This module requires:

- `automation-engine-spring` (provided scope)
- LuaJ 3.0.1 for Lua script execution
