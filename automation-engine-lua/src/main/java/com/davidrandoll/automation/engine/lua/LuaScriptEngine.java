package com.davidrandoll.automation.engine.lua;

import com.davidrandoll.automation.engine.lua.functions.ILuaFunctionContributor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.List;
import java.util.Map;

/**
 * Executes Lua scripts with JSON-based data exchange.
 * <p>
 * All Java objects are normalized to JsonNode via Jackson before
 * entering Lua, and results are converted back the same way.
 */
@Slf4j
@RequiredArgsConstructor
public class LuaScriptEngine {

    private final ObjectMapper mapper;
    private final List<ILuaFunctionContributor> contributors;

    /**
     * Executes a Lua script with the provided variables/bindings.
     *
     * @param script    The Lua script to execute
     * @param variables A map of variables to bind in the Lua environment
     * @return The result of the script execution as a Java object
     */
    public Object execute(String script, Map<String, Object> variables) {
        return executeAs(script, variables, Object.class);
    }

    public <T> T executeAs(String script, Map<String, Object> variables, Class<T> type) {
        JsonNode result = executeInternal(script, variables);
        return mapper.convertValue(result, type);
    }

    public <T> T executeAs(String script, Map<String, Object> variables, TypeReference<T> typeRef) {
        JsonNode result = executeInternal(script, variables);
        return mapper.convertValue(result, typeRef);
    }

    /**
     * Executes a Lua script and returns the result as a boolean.
     *
     * @param script    The Lua script to execute
     * @param variables A map of variables to bind in the Lua environment
     * @return true if the script returns a truthy value, false otherwise
     */
    public boolean executeAsBoolean(String script, Map<String, Object> variables) {
        JsonNode node = executeInternal(script, variables);

        if (node == null || node.isNull()) return false;
        if (node.isBoolean()) return node.booleanValue();
        if (node.isNumber()) return node.doubleValue() != 0;
        if (node.isTextual()) {
            String v = node.textValue();
            return !v.isEmpty() && !"false".equalsIgnoreCase(v) && !"nil".equalsIgnoreCase(v);
        }
        return true;
    }

    /**
     * Executes a Lua script and returns the result as a Map.
     *
     * @param script    The Lua script to execute
     * @param variables A map of variables to bind in the Lua environment
     * @return The result as a Map, or empty map if result is not a table
     */
    public Map<String, Object> executeAsMap(String script, Map<String, Object> variables) {
        return executeAs(script, variables, new TypeReference<>() {
        });
    }

    /* ============================================================
       Core Execution
       ============================================================ */

    private JsonNode executeInternal(String script, Map<String, Object> variables) {
        Globals globals = JsePlatform.standardGlobals();

        // Bind variables (Java → JsonNode → Lua)
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            JsonNode node = mapper.valueToTree(entry.getValue());
            globals.set(entry.getKey(), jsonToLua(node));
        }

        // Apply contributors
        if (contributors != null) {
            for (ILuaFunctionContributor contributor : contributors) {
                contributor.contribute(globals, this);
            }
        }

        try {
            LuaValue chunk = globals.load(script);
            LuaValue result = chunk.call();
            return luaToJson(result);
        } catch (LuaError e) {
            log.error("Lua execution error: {}", e.getMessage(), e);
            throw new LuaScriptExecutionException("Failed to execute Lua script", e);
        }
    }

    /* ============================================================
       JsonNode → LuaValue
       ============================================================ */
    public LuaValue jsonToLua(JsonNode node) {
        if (node == null || node.isNull()) return LuaValue.NIL;
        if (node.isBoolean()) return LuaValue.valueOf(node.booleanValue());
        if (node.isIntegralNumber()) return LuaValue.valueOf(node.longValue());
        if (node.isFloatingPointNumber()) return LuaValue.valueOf(node.doubleValue());
        if (node.isTextual()) return LuaValue.valueOf(node.textValue());

        if (node.isArray()) {
            LuaTable table = new LuaTable();
            int i = 1;
            for (JsonNode element : node) {
                table.set(i++, jsonToLua(element));
            }
            return table;
        }

        if (node.isObject()) {
            LuaTable table = new LuaTable();
            node.fields().forEachRemaining(e ->
                    table.set(e.getKey(), jsonToLua(e.getValue()))
            );
            return table;
        }

        return LuaValue.NIL;
    }

    /* ============================================================
       LuaValue → JsonNode
       ============================================================ */

    public JsonNode luaToJson(LuaValue value) {
        if (value == null || value.isnil()) return mapper.nullNode();
        if (value.isboolean()) return mapper.valueToTree(value.toboolean());
        if (value.isint()) return mapper.valueToTree(value.toint());
        if (value.isnumber()) return mapper.valueToTree(value.todouble());
        if (value.isstring()) return mapper.valueToTree(value.tojstring());
        if (value.istable()) return luaTableToJson(value.checktable());
        return mapper.valueToTree(value.tojstring());
    }

    private JsonNode luaTableToJson(LuaTable table) {
        boolean isArray = true;
        int maxIndex = 0;

        for (LuaValue k = table.next(LuaValue.NIL).arg1();
             !k.isnil();
             k = table.next(k).arg1()) {

            if (!k.isint() || k.toint() <= 0) {
                isArray = false;
                break;
            }
            maxIndex = Math.max(maxIndex, k.toint());
        }

        if (isArray && maxIndex > 0) {
            ArrayNode array = mapper.createArrayNode();
            for (int i = 1; i <= maxIndex; i++) {
                array.add(luaToJson(table.get(i)));
            }
            return array;
        }

        ObjectNode obj = mapper.createObjectNode();
        for (LuaValue k = table.next(LuaValue.NIL).arg1();
             !k.isnil();
             k = table.next(k).arg1()) {

            obj.set(k.tojstring(), luaToJson(table.get(k)));
        }
        return obj;
    }

    /* ============================================================
       Exception
       ============================================================ */
    public static class LuaScriptExecutionException extends RuntimeException {
        public LuaScriptExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}