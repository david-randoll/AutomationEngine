package com.davidrandoll.automation.engine.lua.conditions;

import com.davidrandoll.automation.engine.core.conditions.IConditionContext;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

/**
 * Context for the LuaScript condition.
 * Contains the Lua script that determines whether the condition is met.
 */
@Data
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        LuaScriptConditionContext.Fields.alias,
        LuaScriptConditionContext.Fields.description,
        LuaScriptConditionContext.Fields.script
})
public class LuaScriptConditionContext implements IConditionContext {
    /**
     * Unique identifier for this condition
     */
    private String alias;

    /**
     * Human-readable description of what this condition checks
     */
    private String description;

    /**
     * The Lua script to evaluate.
     * The script has access to 'event', 'metadata', 'log', and 'json' objects.
     * The script should return true if the condition is met, false otherwise.
     */
    @ContextField(
        widget = ContextField.Widget.MONACO_EDITOR,
        monacoLanguage = "lua",
        helpText = "Lua script that returns true/false. Has access to: event, metadata, log, json objects."
    )
    @JsonAlias({"script", "lua", "code"})
    private String script;
}
