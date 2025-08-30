package com.davidrandoll.automation.engine;

import com.davidrandoll.automation.engine.config.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration
@Import({
        BuilderConfig.class,
        ConverterConfig.class,
        CoreConfig.class,
        ModulesConfig.class,
        ParserConfig.class,
        ProviderConfig.class,
        SupplierConfig.class
})
@EnableScheduling
public class AutomationEngineAutoConfiguration {

}