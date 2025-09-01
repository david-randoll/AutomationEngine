package com.davidrandoll.automation.engine.spring.events;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(AESpringEventsConfig.class)
public class AESpringEventsAutoConfiguration {
}