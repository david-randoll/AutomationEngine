package com.davidrandoll.automation.engine.lua.variables;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.lua.LuaScriptEngine;
import com.davidrandoll.automation.engine.spring.spi.PluggableVariable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Variable that computes values using a Lua script.
 * <p>
 * The script should return a table with variable names and values.
 * The returned values are added to the event context metadata for use
 * in subsequent triggers, conditions, and actions.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class LuaScriptVariable extends PluggableVariable<LuaScriptVariableContext> {
    private final LuaScriptEngine luaEngine;

    @Override
    public void resolve(EventContext ec, LuaScriptVariableContext vc) {
        if (!StringUtils.hasText(vc.getScript())) {
            log.warn("LuaScriptVariable skipped: script is null or blank");
            return;
        }

        log.debug("Resolving Lua script variable: {}", vc.getAlias());

        Map<String, Object> bindings = new HashMap<>();
        bindings.put("event", ec.getEventData());
        bindings.put("metadata", ec.getMetadata());

        try {
            Map<String, Object> result = luaEngine.executeAsMap(vc.getScript(), bindings);

            if (!result.isEmpty()) {
                ec.addMetadata(result);
                log.info("LuaScriptVariable '{}' added {} variables to metadata", vc.getAlias(), result.size());
            } else {
                log.debug("LuaScriptVariable '{}' returned no variables", vc.getAlias());
            }
        } catch (LuaScriptEngine.LuaScriptExecutionException e) {
            log.error("LuaScriptVariable failed: {}", e.getMessage());
            throw new RuntimeException("Lua script execution failed for variable: " + vc.getAlias(), e);
        }
    }

    @Override
    public boolean autoEvaluateExpression() {
        return false;
    }
}
