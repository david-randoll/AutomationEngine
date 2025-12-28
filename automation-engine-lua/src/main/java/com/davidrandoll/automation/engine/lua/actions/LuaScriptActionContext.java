package com.davidrandoll.automation.engine.lua.actions;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

/**
 * Context for the LuaScript action.
 * Contains the Lua script to execute as part of an automation action.
 */
@Data
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        LuaScriptActionContext.Fields.alias,
        LuaScriptActionContext.Fields.description,
        LuaScriptActionContext.Fields.script
})
public class LuaScriptActionContext implements IActionContext {
    /**
     * Unique identifier for this action
     */
    private String alias;

    /**
     * Human-readable description of what this action does
     */
    private String description;

    /**
     * The Lua script to execute.
     * The script has access to 'event', 'metadata', 'log', 'json', and 'result' objects.
     * Values set in the 'result' table will be added to the event context metadata.
     */
    @ContextField(
        widget = ContextField.Widget.MONACO_EDITOR,
        monacoLanguage = "lua",
        helpText = "Lua script with access to: event, metadata, log, json, result. Values in 'result' table are stored in context."
    )
    @JsonAlias({"script", "lua", "code"})
    private String script;

    @ContextField(
        placeholder = "lua",
        helpText = "Variable name to store the script result. Access via {{ lua.fieldName }}"
    )
    private String storeToVariable = "lua";
}
