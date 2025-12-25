package com.davidrandoll.automation.engine.lua.conditions;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.lua.LuaScriptEngine;
import com.davidrandoll.automation.engine.spring.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Condition that evaluates a Lua script to determine if the automation should proceed.
 * <p>
 * The script should return a truthy value if the condition is met.
 * Has access to event data, metadata, logging, and JSON utilities.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class LuaScriptCondition extends PluggableCondition<LuaScriptConditionContext> {
    private final LuaScriptEngine luaEngine;

    @Override
    public boolean isSatisfied(EventContext ec, LuaScriptConditionContext cc) {
        if (!StringUtils.hasText(cc.getScript())) {
            log.warn("LuaScriptCondition skipped: script is null or blank, returning false");
            return false;
        }

        log.debug("Evaluating Lua script condition: {}", cc.getAlias());

        Map<String, Object> bindings = new HashMap<>(ec.getEventData());

        try {
            boolean result = luaEngine.executeAsBoolean(cc.getScript(), bindings);
            log.debug("LuaScriptCondition '{}' evaluated to: {}", cc.getAlias(), result);
            return result;
        } catch (LuaScriptEngine.LuaScriptExecutionException e) {
            log.error("LuaScriptCondition failed: {}", e.getMessage());
            throw new RuntimeException("Lua script execution failed for condition: " + cc.getAlias(), e);
        }
    }

    @Override
    public boolean autoEvaluateExpression() {
        return false;
    }
}
