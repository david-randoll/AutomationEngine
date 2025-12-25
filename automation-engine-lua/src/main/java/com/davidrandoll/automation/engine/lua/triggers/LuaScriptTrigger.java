package com.davidrandoll.automation.engine.lua.triggers;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.lua.LuaScriptEngine;
import com.davidrandoll.automation.engine.spring.spi.PluggableTrigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Trigger that evaluates a Lua script to determine if the automation should run.
 * <p>
 * The script should return a truthy value to trigger the automation.
 * Has access to event data, metadata, logging, and JSON utilities.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class LuaScriptTrigger extends PluggableTrigger<LuaScriptTriggerContext> {
    private final LuaScriptEngine luaEngine;

    @Override
    public boolean isTriggered(EventContext ec, LuaScriptTriggerContext tc) {
        if (!StringUtils.hasText(tc.getScript())) {
            log.warn("LuaScriptTrigger skipped: script is null or blank");
            return false;
        }

        log.debug("Evaluating Lua script trigger: {}", tc.getAlias());

        Map<String, Object> bindings = new HashMap<>(ec.getEventData());

        try {
            boolean result = luaEngine.executeAsBoolean(tc.getScript(), bindings);
            log.debug("LuaScriptTrigger '{}' evaluated to: {}", tc.getAlias(), result);
            return result;
        } catch (LuaScriptEngine.LuaScriptExecutionException e) {
            log.error("LuaScriptTrigger failed: {}", e.getMessage());
            throw new RuntimeException("Lua script execution failed for trigger: " + tc.getAlias(), e);
        }
    }

    @Override
    public boolean autoEvaluateExpression() {
        return false;
    }
}
