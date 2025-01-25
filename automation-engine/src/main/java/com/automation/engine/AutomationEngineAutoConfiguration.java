package com.automation.engine;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan(basePackages = {"com.automation.engine"})
@ConfigurationPropertiesScan(basePackages = {"com.automation.engine"})
public class AutomationEngineAutoConfiguration {
}