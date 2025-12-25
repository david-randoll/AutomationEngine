package com.davidrandoll.automation.engine.lua.results;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.lua.LuaScriptEngine;
import com.davidrandoll.automation.engine.spring.spi.PluggableResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Result that generates the automation output using a Lua script.
 * <p>
 * The script should return a value that will be converted to JSON
 * and used as the automation's execution result.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class LuaScriptResult extends PluggableResult<LuaScriptResultContext> {
    private final LuaScriptEngine luaEngine;
    private final ObjectMapper objectMapper;

    @Override
    public JsonNode getExecutionSummary(EventContext ec, LuaScriptResultContext rc) {
        if (!StringUtils.hasText(rc.getScript())) {
            log.warn("LuaScriptResult skipped: script is null or blank");
            return objectMapper.nullNode();
        }

        log.debug("Generating Lua script result: {}", rc.getAlias());

        Map<String, Object> bindings = new HashMap<>();
        bindings.put("event", ec.getEventData());
        bindings.put("metadata", ec.getMetadata());

        try {
            Object result = luaEngine.execute(rc.getScript(), bindings);
            log.debug("LuaScriptResult '{}' generated result", rc.getAlias());
            return objectMapper.valueToTree(result);
        } catch (LuaScriptEngine.LuaScriptExecutionException e) {
            log.error("LuaScriptResult failed: {}", e.getMessage());
            throw new RuntimeException("Lua script execution failed for result: " + rc.getAlias(), e);
        }
    }

    @Override
    public boolean autoEvaluateExpression() {
        return false;
    }
}
