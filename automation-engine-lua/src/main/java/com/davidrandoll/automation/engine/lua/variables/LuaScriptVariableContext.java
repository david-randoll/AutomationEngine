package com.davidrandoll.automation.engine.lua.variables;

import com.davidrandoll.automation.engine.core.variables.IVariableContext;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

/**
 * Context for the LuaScript variable.
 * Contains the Lua script that computes variable values.
 */
@Data
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        LuaScriptVariableContext.Fields.alias,
        LuaScriptVariableContext.Fields.description,
        LuaScriptVariableContext.Fields.script
})
public class LuaScriptVariableContext implements IVariableContext {
    /**
     * Unique identifier for this variable
     */
    private String alias;

    /**
     * Human-readable description of what this variable computes
     */
    private String description;

    @ContextField(
        placeholder = "myVariable",
        helpText = "Name of the variable to store the computed value"
    )
    @JsonAlias({"name", "variableName"})
    private String name;

    /**
     * The Lua script to execute.
     * The script has access to 'event', 'metadata', 'log', and 'json' objects.
     * The script should return a table with variable names and values that will be
     * added to the event context metadata.
     */
    @ContextField(
        widget = ContextField.Widget.MONACO_EDITOR,
        monacoLanguage = "lua",
        helpText = "Lua script that returns a table of variable names/values. Has access to: event, metadata, log, json objects."
    )
    @JsonAlias({"script", "lua", "code"})
    private String script;
}
