package com.automation.engine;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration
@ComponentScan
@ConfigurationPropertiesScan
@EnableScheduling
public class AutomationEngineAutoConfiguration {

}