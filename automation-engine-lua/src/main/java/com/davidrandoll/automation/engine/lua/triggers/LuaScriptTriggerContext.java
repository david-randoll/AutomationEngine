package com.davidrandoll.automation.engine.lua.triggers;

import com.davidrandoll.automation.engine.core.triggers.ITriggerContext;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

/**
 * Context for the LuaScript trigger.
 * Contains the Lua script that determines whether the automation should be triggered.
 */
@Data
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        LuaScriptTriggerContext.Fields.alias,
        LuaScriptTriggerContext.Fields.description,
        LuaScriptTriggerContext.Fields.script
})
public class LuaScriptTriggerContext implements ITriggerContext {
    /**
     * Unique identifier for this trigger
     */
    private String alias;

    /**
     * Human-readable description of what this trigger responds to
     */
    private String description;

    /**
     * The Lua script to evaluate.
     * The script has access to 'event', 'metadata', 'log', and 'json' objects.
     * The script should return true to trigger the automation, false otherwise.
     */
    @ContextField(
        widget = ContextField.Widget.MONACO_EDITOR,
        monacoLanguage = "lua",
        helpText = "Lua script that returns true to trigger automation. Has access to: event, metadata, log, json objects."
    )
    @JsonAlias({"script", "lua", "code"})
    private String script;
}
