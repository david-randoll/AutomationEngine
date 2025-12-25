package com.davidrandoll.automation.engine.lua.functions;

import com.davidrandoll.automation.engine.lua.LuaScriptEngine;
import com.fasterxml.jackson.databind.JsonNode;
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

        /* -----------------------------
           json.encode(value)
           ----------------------------- */
        jsonTable.set("encode", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                try {
                    // Lua → JsonNode
                    JsonNode node = engine.luaToJson(arg);

                    // JsonNode → JSON string
                    String json = mapper.writeValueAsString(node);
                    return LuaValue.valueOf(json);
                } catch (Exception e) {
                    log.error("JSON encode error", e);
                    return LuaValue.NIL;
                }
            }
        });

        /* -----------------------------
           json.decode(string)
           ----------------------------- */
        jsonTable.set("decode", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                try {
                    String json = arg.tojstring();

                    // JSON string → JsonNode
                    JsonNode node = mapper.readTree(json);

                    // JsonNode → Lua
                    return engine.jsonToLua(node);
                } catch (Exception e) {
                    log.error("JSON decode error", e);
                    return LuaValue.NIL;
                }
            }
        });

        globals.set("json", jsonTable);
    }
}