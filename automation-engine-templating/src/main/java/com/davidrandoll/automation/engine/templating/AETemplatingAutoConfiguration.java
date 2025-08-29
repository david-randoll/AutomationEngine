package com.davidrandoll.automation.engine.templating;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(AETemplatingConfig.class)
public class AETemplatingAutoConfiguration {
}