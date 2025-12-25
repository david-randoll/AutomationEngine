package com.davidrandoll.automation.engine.lua;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

/**
 * Contributes JSON utilities to the Lua environment.
 */
@Slf4j
@RequiredArgsConstructor
public class JsonLuaFunctionContributor implements ILuaFunctionContributor {
    private final ObjectMapper mapper;

    @Override
    public void contribute(Globals globals, LuaScriptEngine engine) {
        LuaTable jsonTable = new LuaTable();

        jsonTable.set("encode", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                try {
                    Object javaValue = engine.fromLuaValue(arg);
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
                    return engine.toLuaValue(value);
                } catch (Exception e) {
                    log.error("JSON decode error: {}", e.getMessage());
                    return LuaValue.NIL;
                }
            }
        });

        globals.set("json", jsonTable);
    }
}
