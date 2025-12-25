package com.davidrandoll.automation.engine;

import com.davidrandoll.automation.engine.spring.AESpringConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({AESpringConfig.class, TracingConfig.class})
public class AEAutoConfiguration {
}