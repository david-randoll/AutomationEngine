package com.davidrandoll.automation.engine.lua;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for the Automation Engine Lua module.
 * Enables Lua scripting capabilities for automations.
 */
@AutoConfiguration
@Import(AELuaConfig.class)
public class AELuaAutoConfiguration {
}
