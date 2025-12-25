package com.davidrandoll.automation.engine.lua.actions;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
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
    @JsonAlias({"script", "lua", "code"})
    private String script;
}
