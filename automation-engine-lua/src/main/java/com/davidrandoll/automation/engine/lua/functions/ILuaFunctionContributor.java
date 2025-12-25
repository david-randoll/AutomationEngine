package com.davidrandoll.automation.engine.lua.functions;

import com.davidrandoll.automation.engine.lua.LuaScriptEngine;
import org.luaj.vm2.Globals;

/**
 * Interface for contributing custom functions or tables to the Lua environment.
 * Implementations of this interface can be registered as Spring beans to extend
 * the capabilities of Lua scripts in the Automation Engine.
 */
public interface ILuaFunctionContributor {
    /**
     * Contributes functions or tables to the provided Lua Globals.
     *
     * @param globals The Lua Globals object
     * @param engine  The LuaScriptEngine instance, providing access to conversion utilities
     */
    void contribute(Globals globals, LuaScriptEngine engine);
}
