package com.davidrandoll.automation.engine.lua.actions;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.lua.LuaScriptEngine;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Action that executes a Lua script.
 * <p>
 * The script has access to event data, metadata, logging, and a result table.
 * Values stored in the 'result' table are added to the event context metadata
 * for use by subsequent actions.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class LuaScriptAction extends PluggableAction<LuaScriptActionContext> {
    private final LuaScriptEngine luaEngine;

    @Override
    public void doExecute(EventContext ec, LuaScriptActionContext ac) {
        if (!StringUtils.hasText(ac.getScript())) {
            log.warn("LuaScriptAction skipped: script is null or blank");
            return;
        }

        log.debug("Executing Lua script action: {}", ac.getAlias());

        // Build bindings for the Lua script
        Map<String, Object> bindings = new HashMap<>(ec.getEventData());

        try {
            // Execute the script
            Object scriptResult = luaEngine.execute(ac.getScript(), bindings);

            ec.addMetadata(ac.getStoreToVariable(), scriptResult);

            log.info("LuaScriptAction executed successfully: {}", ac.getAlias());
        } catch (LuaScriptEngine.LuaScriptExecutionException e) {
            log.error("LuaScriptAction failed: {}", e.getMessage());
            throw new RuntimeException("Lua script execution failed for action: " + ac.getAlias(), e);
        }
    }

    @Override
    public boolean autoEvaluateExpression() {
        return false; // We handle our own variable resolution in Lua
    }
}
