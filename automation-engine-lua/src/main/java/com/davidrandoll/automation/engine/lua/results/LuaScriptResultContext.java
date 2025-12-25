package com.davidrandoll.automation.engine.lua.results;

import com.davidrandoll.automation.engine.core.result.IResultContext;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

/**
 * Context for the LuaScript result.
 * Contains the Lua script that generates the automation result.
 */
@Data
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        LuaScriptResultContext.Fields.alias,
        LuaScriptResultContext.Fields.description,
        LuaScriptResultContext.Fields.script
})
public class LuaScriptResultContext implements IResultContext {
    /**
     * Unique identifier for this result
     */
    private String alias;

    /**
     * Human-readable description of what this result represents
     */
    private String description;

    /**
     * The Lua script to execute.
     * The script has access to 'event', 'metadata', 'log', and 'json' objects.
     * The script should return a value (table, string, number, etc.) that will be
     * the result of the automation execution.
     */
    @JsonAlias({"script", "lua", "code"})
    private String script;
}
