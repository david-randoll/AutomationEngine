package com.davidrandoll.automation.engine.spring;

import com.davidrandoll.automation.engine.spring.config.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Import({
        BuilderConfig.class,
        ConverterConfig.class,
        CoreConfig.class,
        ModulesConfig.class,
        ParserConfig.class,
        ProviderConfig.class,
        SupplierConfig.class,
        TracingConfig.class
})
@EnableScheduling
public class AESpringConfig {
}