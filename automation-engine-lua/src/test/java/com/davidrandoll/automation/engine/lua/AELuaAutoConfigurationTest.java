package com.davidrandoll.automation.engine.lua;

import com.davidrandoll.automation.engine.lua.actions.LuaScriptAction;
import com.davidrandoll.automation.engine.lua.conditions.LuaScriptCondition;
import com.davidrandoll.automation.engine.lua.functions.JsonLuaFunctionContributor;
import com.davidrandoll.automation.engine.lua.functions.LogLuaFunctionContributor;
import com.davidrandoll.automation.engine.lua.results.LuaScriptResult;
import com.davidrandoll.automation.engine.lua.triggers.LuaScriptTrigger;
import com.davidrandoll.automation.engine.lua.variables.LuaScriptVariable;
import com.davidrandoll.automation.engine.test.AutomationEngineApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that all Lua infrastructure beans are properly configured and available.
 * These beans enable Lua scripting throughout the automation engine.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AutomationEngineApplication.class)
class AELuaAutoConfigurationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void shouldConfigureLuaScriptEngine() {
        LuaScriptEngine bean = context.getBean(LuaScriptEngine.class);
        assertThat(bean).isNotNull()
                .as("LuaScriptEngine should be configured to execute Lua scripts");
    }

    @Test
    void shouldConfigureLogLuaFunctionContributor() {
        LogLuaFunctionContributor bean = context.getBean(LogLuaFunctionContributor.class);
        assertThat(bean).isNotNull()
                .as("LogLuaFunctionContributor should be configured to provide logging to Lua");
    }

    @Test
    void shouldConfigureJsonLuaFunctionContributor() {
        JsonLuaFunctionContributor bean = context.getBean(JsonLuaFunctionContributor.class);
        assertThat(bean).isNotNull()
                .as("JsonLuaFunctionContributor should be configured to provide JSON utilities to Lua");
    }

    @Test
    void shouldConfigureLuaScriptAction() {
        LuaScriptAction bean = context.getBean(LuaScriptAction.class);
        assertThat(bean).isNotNull()
                .as("LuaScriptAction should be configured to execute Lua scripts as actions");
    }

    @Test
    void shouldConfigureLuaScriptTrigger() {
        LuaScriptTrigger bean = context.getBean(LuaScriptTrigger.class);
        assertThat(bean).isNotNull()
                .as("LuaScriptTrigger should be configured to evaluate Lua scripts for triggering");
    }

    @Test
    void shouldConfigureLuaScriptCondition() {
        LuaScriptCondition bean = context.getBean(LuaScriptCondition.class);
        assertThat(bean).isNotNull()
                .as("LuaScriptCondition should be configured to evaluate Lua scripts as conditions");
    }

    @Test
    void shouldConfigureLuaScriptVariable() {
        LuaScriptVariable bean = context.getBean(LuaScriptVariable.class);
        assertThat(bean).isNotNull()
                .as("LuaScriptVariable should be configured to compute variables via Lua scripts");
    }

    @Test
    void shouldConfigureLuaScriptResult() {
        LuaScriptResult bean = context.getBean(LuaScriptResult.class);
        assertThat(bean).isNotNull()
                .as("LuaScriptResult should be configured to generate results via Lua scripts");
    }
}
