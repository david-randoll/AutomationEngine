package com.davidrandoll.automation.engine.spring.tx;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(AESpringTxConfig.class)
public class AESpringTxAutoConfiguration {
}
