package com.davidrandoll.automation.engine.spring.web;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({AESpringWebConfig.class})
public class AESpringWebAutoConfiguration {

}