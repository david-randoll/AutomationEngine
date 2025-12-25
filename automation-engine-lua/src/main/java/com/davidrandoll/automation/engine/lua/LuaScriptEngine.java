package com.davidrandoll.automation.engine.lua;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.*;

/**
 * A service for executing Lua scripts with access to event data.
 * <p>
 * This class provides methods to execute Lua scripts with bindings
 * for event data, metadata, logging, and other utilities.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class LuaScriptEngine {
    private final ObjectMapper mapper;

    /**
     * Executes a Lua script with the provided variables/bindings.
     *
     * @param script    The Lua script to execute
     * @param variables A map of variables to bind in the Lua environment
     * @return The result of the script execution as a Java object
     */
    public Object execute(String script, Map<String, Object> variables) {
        Globals globals = JsePlatform.standardGlobals();

        // Bind all variables to the Lua environment
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            globals.set(entry.getKey(), toLuaValue(entry.getValue()));
        }

        // Add logging support
        globals.set("log", createLogTable());

        // Add JSON utilities
        globals.set("json", createJsonTable());

        try {
            LuaValue chunk = globals.load(script);
            LuaValue result = chunk.call();
            return fromLuaValue(result);
        } catch (LuaError e) {
            log.error("Lua script execution error: {}", e.getMessage());
            throw new LuaScriptExecutionException("Failed to execute Lua script", e);
        }
    }

    /**
     * Executes a Lua script and returns the result as a boolean.
     *
     * @param script    The Lua script to execute
     * @param variables A map of variables to bind in the Lua environment
     * @return true if the script returns a truthy value, false otherwise
     */
    public boolean executeAsBoolean(String script, Map<String, Object> variables) {
        Object result = execute(script, variables);
        if (result == null) {
            return false;
        }
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        if (result instanceof Number) {
            return ((Number) result).doubleValue() != 0;
        }
        if (result instanceof String) {
            return !((String) result).isEmpty() &&
                    !result.equals("false") &&
                    !result.equals("nil");
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
    @SuppressWarnings("unchecked")
    public Map<String, Object> executeAsMap(String script, Map<String, Object> variables) {
        Object result = execute(script, variables);
        if (result instanceof Map) {
            return (Map<String, Object>) result;
        }
        return Collections.emptyMap();
    }

    /**
     * Converts a Java object to a LuaValue.
     */
    @SuppressWarnings("unchecked")
    private LuaValue toLuaValue(Object obj) {
        if (obj == null) {
            return LuaValue.NIL;
        }
        if (obj instanceof Boolean) {
            return LuaValue.valueOf((Boolean) obj);
        }
        if (obj instanceof Number) {
            if (obj instanceof Double || obj instanceof Float) {
                return LuaValue.valueOf(((Number) obj).doubleValue());
            }
            return LuaValue.valueOf(((Number) obj).longValue());
        }
        if (obj instanceof String) {
            return LuaValue.valueOf((String) obj);
        }
        if (obj instanceof Map) {
            return mapToLuaTable((Map<String, Object>) obj);
        }
        if (obj instanceof List) {
            return listToLuaTable((List<?>) obj);
        }
        if (obj instanceof JsonNode) {
            return jsonNodeToLuaValue((JsonNode) obj);
        }
        // For other objects, try to convert to map using Jackson
        try {
            Map<String, Object> map = mapper.convertValue(obj, new TypeReference<>() {
            });
            return mapToLuaTable(map);
        } catch (Exception e) {
            log.debug("Could not convert object to Lua table, using string representation: {}", obj.getClass());
            return LuaValue.valueOf(obj.toString());
        }
    }

    private LuaTable mapToLuaTable(Map<String, Object> map) {
        LuaTable table = new LuaTable();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            table.set(entry.getKey(), toLuaValue(entry.getValue()));
        }
        return table;
    }

    private LuaTable listToLuaTable(List<?> list) {
        LuaTable table = new LuaTable();
        for (int i = 0; i < list.size(); i++) {
            table.set(i + 1, toLuaValue(list.get(i))); // Lua arrays are 1-indexed
        }
        return table;
    }

    private LuaValue jsonNodeToLuaValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return LuaValue.NIL;
        }
        if (node.isBoolean()) {
            return LuaValue.valueOf(node.booleanValue());
        }
        if (node.isNumber()) {
            if (node.isFloatingPointNumber()) {
                return LuaValue.valueOf(node.doubleValue());
            }
            return LuaValue.valueOf(node.longValue());
        }
        if (node.isTextual()) {
            return LuaValue.valueOf(node.textValue());
        }
        if (node.isArray()) {
            LuaTable table = new LuaTable();
            int index = 1;
            for (JsonNode element : node) {
                table.set(index++, jsonNodeToLuaValue(element));
            }
            return table;
        }
        if (node.isObject()) {
            LuaTable table = new LuaTable();
            node.fieldNames().forEachRemaining(field ->
                    table.set(field, jsonNodeToLuaValue(node.get(field)))
            );
            return table;
        }
        return LuaValue.NIL;
    }

    /**
     * Converts a LuaValue back to a Java object.
     */
    private Object fromLuaValue(LuaValue value) {
        if (value == null || value.isnil()) {
            return null;
        }
        if (value.isboolean()) {
            return value.toboolean();
        }
        if (value.isint()) {
            return value.toint();
        }
        if (value.isnumber()) {
            return value.todouble();
        }
        if (value.isstring()) {
            return value.tojstring();
        }
        if (value.istable()) {
            return luaTableToMap(value.checktable());
        }
        return value.tojstring();
    }

    private Object luaTableToMap(LuaTable table) {
        // Check if it's an array (sequential integer keys starting from 1)
        boolean isArray = true;
        int maxKey = 0;
        for (LuaValue key = table.next(LuaValue.NIL).arg1(); !key.isnil(); key = table.next(key).arg1()) {
            if (!key.isint() || key.toint() <= 0) {
                isArray = false;
                break;
            }
            maxKey = Math.max(maxKey, key.toint());
        }

        if (isArray && maxKey > 0) {
            List<Object> list = new ArrayList<>();
            for (int i = 1; i <= maxKey; i++) {
                LuaValue val = table.get(i);
                if (val.isnil()) {
                    isArray = false;
                    break;
                }
                list.add(fromLuaValue(val));
            }
            if (isArray) {
                return list;
            }
        }

        // Convert to map
        Map<String, Object> map = new LinkedHashMap<>();
        for (LuaValue key = table.next(LuaValue.NIL).arg1(); !key.isnil(); key = table.next(key).arg1()) {
            String keyStr = key.isstring() ? key.tojstring() : String.valueOf(key.toint());
            map.put(keyStr, fromLuaValue(table.get(key)));
        }
        return map;
    }

    private LuaTable createLogTable() {
        LuaTable logTable = new LuaTable();

        logTable.set("info", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                log.info("[Lua] {}", arg.tojstring());
                return LuaValue.NIL;
            }
        });

        logTable.set("warn", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                log.warn("[Lua] {}", arg.tojstring());
                return LuaValue.NIL;
            }
        });

        logTable.set("error", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                log.error("[Lua] {}", arg.tojstring());
                return LuaValue.NIL;
            }
        });

        logTable.set("debug", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                log.debug("[Lua] {}", arg.tojstring());
                return LuaValue.NIL;
            }
        });

        return logTable;
    }

    private LuaTable createJsonTable() {
        LuaTable jsonTable = new LuaTable();

        // Create reference to this engine for use in anonymous classes
        final LuaScriptEngine self = this;

        jsonTable.set("encode", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                try {
                    Object javaValue = self.fromLuaValue(arg);
                    String json = mapper.writeValueAsString(javaValue);
                    return LuaValue.valueOf(json);
                } catch (Exception e) {
                    log.error("JSON encode error: {}", e.getMessage());
                    return LuaValue.NIL;
                }
            }
        });

        jsonTable.set("decode", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                try {
                    String json = arg.tojstring();
                    Object value = mapper.readValue(json, Object.class);
                    return self.toLuaValue(value);
                } catch (Exception e) {
                    log.error("JSON decode error: {}", e.getMessage());
                    return LuaValue.NIL;
                }
            }
        });

        return jsonTable;
    }

    /**
     * Exception thrown when Lua script execution fails.
     */
    public static class LuaScriptExecutionException extends RuntimeException {
        public LuaScriptExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
