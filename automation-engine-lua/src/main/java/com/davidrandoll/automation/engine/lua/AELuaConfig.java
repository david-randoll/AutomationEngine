package com.davidrandoll.automation.engine.lua;

import com.davidrandoll.automation.engine.lua.actions.LuaScriptAction;
import com.davidrandoll.automation.engine.lua.conditions.LuaScriptCondition;
import com.davidrandoll.automation.engine.lua.results.LuaScriptResult;
import com.davidrandoll.automation.engine.lua.triggers.LuaScriptTrigger;
import com.davidrandoll.automation.engine.lua.variables.LuaScriptVariable;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that creates all Lua-related beans.
 */
@Configuration
public class AELuaConfig {

    @Bean
    @ConditionalOnMissingBean
    public LuaScriptEngine luaScriptEngine(ObjectMapper objectMapper) {
        return new LuaScriptEngine(objectMapper);
    }

    @Bean("luaScript")
    @ConditionalOnMissingBean(name = "luaScript", ignored = LuaScriptAction.class)
    public LuaScriptAction luaScriptAction(LuaScriptEngine luaEngine) {
        return new LuaScriptAction(luaEngine);
    }

    @Bean("luaScriptTrigger")
    @ConditionalOnMissingBean(name = "luaScriptTrigger", ignored = LuaScriptTrigger.class)
    public LuaScriptTrigger luaScriptTrigger(LuaScriptEngine luaEngine) {
        return new LuaScriptTrigger(luaEngine);
    }

    @Bean("luaScriptCondition")
    @ConditionalOnMissingBean(name = "luaScriptCondition", ignored = LuaScriptCondition.class)
    public LuaScriptCondition luaScriptCondition(LuaScriptEngine luaEngine) {
        return new LuaScriptCondition(luaEngine);
    }

    @Bean("luaScriptVariable")
    @ConditionalOnMissingBean(name = "luaScriptVariable", ignored = LuaScriptVariable.class)
    public LuaScriptVariable luaScriptVariable(LuaScriptEngine luaEngine) {
        return new LuaScriptVariable(luaEngine);
    }

    @Bean("luaScriptResult")
    @ConditionalOnMissingBean(name = "luaScriptResult", ignored = LuaScriptResult.class)
    public LuaScriptResult luaScriptResult(LuaScriptEngine luaEngine, ObjectMapper objectMapper) {
        return new LuaScriptResult(luaEngine, objectMapper);
    }
}
