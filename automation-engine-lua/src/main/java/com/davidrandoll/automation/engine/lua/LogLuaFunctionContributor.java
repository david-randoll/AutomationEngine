package com.davidrandoll.automation.engine.lua;

import lombok.extern.slf4j.Slf4j;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

/**
 * Contributes logging functions to the Lua environment.
 */
@Slf4j
public class LogLuaFunctionContributor implements ILuaFunctionContributor {
    @Override
    public void contribute(Globals globals, LuaScriptEngine engine) {
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

        globals.set("log", logTable);
    }
}
