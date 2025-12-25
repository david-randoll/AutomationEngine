package com.davidrandoll.automation.engine.lua;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
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
    private final List<ILuaFunctionContributor> contributors;

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

        // Apply all contributors
        if (contributors != null) {
            for (ILuaFunctionContributor contributor : contributors) {
                contributor.contribute(globals, this);
            }
        }

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
        return switch (result) {
            case null -> false;
            case Boolean b -> b;
            case Number number -> number.doubleValue() != 0;
            case String s -> !s.isEmpty() &&
                             !result.equals("false") &&
                             !result.equals("nil");
            default -> true;
        };
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
    public LuaValue toLuaValue(Object obj) {
        switch (obj) {
            case null -> {
                return LuaValue.NIL;
            }
            case Boolean b -> {
                return LuaValue.valueOf(b);
            }
            case Number number -> {
                if (obj instanceof Double || obj instanceof Float) {
                    return LuaValue.valueOf(number.doubleValue());
                }
                return LuaValue.valueOf(number.longValue());
            }
            case String s -> {
                return LuaValue.valueOf(s);
            }
            case Map<?, ?> map -> {
                return mapToLuaTable((Map<String, Object>) map);
            }
            case List<?> list -> {
                return listToLuaTable(list);
            }
            case JsonNode jsonNode -> {
                return jsonNodeToLuaValue(jsonNode);
            }
            default -> {
            }
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
    public Object fromLuaValue(LuaValue value) {
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

    /**
     * Exception thrown when Lua script execution fails.
     */
    public static class LuaScriptExecutionException extends RuntimeException {
        public LuaScriptExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
