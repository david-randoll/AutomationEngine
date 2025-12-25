# Automation Engine Lua Module

The `automation-engine-lua` module provides powerful scripting capabilities to the Automation Engine using the Lua
language. It allows you to write custom logic for triggers, conditions, actions, variables, and results without needing
to write and compile Java code.

## Table of Contents

- [Getting Started](#getting-started)
- [Lua Environment](#lua-environment)
  - [Available Variables](#available-variables)
  - [Built-in Functions](#built-in-functions)
- [Components](#components)
  - [Lua Script Trigger](#lua-script-trigger)
  - [Lua Script Condition](#lua-script-condition)
  - [Lua Script Action](#lua-script-action)
  - [Lua Script Variable](#lua-script-variable)
  - [Lua Script Result](#lua-script-result)
- [Tips for New Users](#tips-for-new-users)

---

## Getting Started

To use Lua scripting in your project, add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.davidrandoll</groupId>
    <artifactId>automation-engine-lua</artifactId>
    <version>${automation-engine.version}</version>
</dependency>
```

The module is auto-configured, so once the dependency is added, the Lua components will be available for use in your
automation definitions.

---

## Lua Environment

When a Lua script is executed, it runs in a sandbox with access to the following:

### Available Variables

All data from the current `EventContext` is injected directly into the Lua global scope. This includes:

- **Event Fields**: If your event has a field named `username`, it is available as `username` in Lua.
- **Metadata**: Any metadata added by previous variables or actions is also available by its key.

Example: If an event has a `price` of 100 and a `quantity` of 5:

```lua
local total = price * quantity
return total > 400
```

### Built-in Functions

#### Logging (`log`)

You can use the `log` table to output messages to the application logs.

- `log.info(message)`
- `log.warn(message)`
- `log.error(message)`
- `log.debug(message)`

```lua
log.info("Processing order for " .. customerName)
```

#### JSON Utilities (`json`)

The `json` table provides methods to work with JSON strings.

- `json.encode(table)`: Converts a Lua table/value to a JSON string.
- `json.decode(string)`: Converts a JSON string to a Lua table/value.

```lua
local data = json.decode(jsonString)
log.info("User ID: " .. data.id)
```

#### Extending the Lua Environment
You can add your own custom functions or tables to the Lua environment by implementing the `ILuaFunctionContributor` interface and registering it as a Spring bean.

```java
@Component
public class MyCustomContributor implements ILuaFunctionContributor {
    @Override
    public void contribute(Globals globals, LuaScriptEngine engine) {
        globals.set("myFunc", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                String input = arg.tojstring();
                return LuaValue.valueOf("Processed: " .. input);
            }
        });
    }
}
```

#### Standard Lua Libraries

The environment includes standard Lua libraries:

- `base`: Basic functions like `tostring`, `tonumber`, `print`, `type`, etc.
- `table`: Table manipulation functions.
- `string`: String manipulation functions.
- `math`: Mathematical functions.
- `os`: Operating system facilities (e.g., `os.date`, `os.time`).

---

## Components

### Lua Script Trigger

**Identifier**: `luaScript`

Evaluates a Lua script to determine if an automation should be triggered. The script should return a truthy value
(anything except `false` or `nil`) to activate the trigger.

**Parameters**:

- `script` (or `lua`, `code`): The Lua script to execute.

**Example**:

```yaml
triggers:
  - trigger: luaScript
    script: |
      return temperature > 30 and humidity > 80
```

### Lua Script Condition

**Identifier**: `luaScript`

Evaluates a Lua script to determine if the automation should proceed. Similar to the trigger, it must return a truthy
value.

**Parameters**:

- `script` (or `lua`, `code`): The Lua script to execute.

**Example**:

```yaml
conditions:
  - condition: luaScript
    lua: |
      return user.role == "ADMIN" or user.isVip
```

### Lua Script Action

**Identifier**: `luaScript`

Executes a Lua script as an action. The return value of the script is stored in the event metadata.

**Parameters**:

- `script` (or `lua`, `code`): The Lua script to execute.
- `storeToVariable`: The name of the metadata key to store the result in (defaults to `lua`).

**Example**:

```yaml
actions:
  - action: luaScript
    storeToVariable: "calculation"
    script: |
      local base = price * 0.9
      return {
        discountedPrice = base,
        tax = base * 0.15
      }
```

### Lua Script Variable

**Identifier**: `luaScript`

Computes values using a Lua script and adds them to the event metadata.

**Parameters**:

- `script` (or `lua`, `code`): The Lua script to execute.
- `name` (optional): If provided, the entire result of the script is stored under this key. If omitted and the script
  returns a table, each key-value pair in the table is added to the metadata individually.

**Example**:

```yaml
variables:
  - variable: luaScript
    script: |
      return {
        isWeekend = os.date("%w") == "0" or os.date("%w") == "6",
        processedAt = os.date("!%Y-%m-%dT%H:%M:%SZ")
      }
```

### Lua Script Result

**Identifier**: `luaScript`

Generates the final execution summary of the automation using a Lua script.

**Parameters**:

- `script` (or `lua`, `code`): The Lua script to execute.

**Example**:

```yaml
result:
  type: luaScript
  script: |
    return {
      status = "SUCCESS",
      message = "Processed " .. count .. " items",
      timestamp = os.time()
    }
```

---

## Tips for New Users

1.  **1-Based Indexing**: Lua arrays (tables used as lists) start at index `1`, not `0`.
2.  **Truthy Values**: In Lua, only `false` and `nil` are considered falsy. The number `0` and empty strings `""` are
    considered **true**.
3.  **String Concatenation**: Use `..` to concatenate strings (e.g., `"Hello " .. name`).
4.  **Tables**: Tables are the only data structure in Lua. They can be used as both arrays `{1, 2, 3}` and dictionaries
    `{key = "value"}`.
5.  **Global Scope**: Be careful with global variables. Use the `local` keyword for variables that should only exist
    within your script.
6.  **Returning Values**: Always use the `return` statement if you want to pass data back to the Automation Engine.
