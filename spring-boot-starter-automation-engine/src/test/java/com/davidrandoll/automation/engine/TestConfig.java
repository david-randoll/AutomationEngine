package com.davidrandoll.automation.engine;

import com.davidrandoll.automation.engine.provider.AEConfigProvider;
import io.pebbletemplates.boot.autoconfigure.PebbleProperties;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.attributes.methodaccess.MethodAccessValidator;
import io.pebbletemplates.pebble.extension.Extension;
import io.pebbletemplates.pebble.loader.Loader;
import io.pebbletemplates.spring.extension.SpringExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.util.List;

@Configuration
public class TestConfig {
    @Bean
    public AEConfigProvider automationEngineConfigProvider(ThreadPoolTaskScheduler taskScheduler) {
        return new AEConfigProvider()
                .setTaskScheduler(taskScheduler)
                .setDefaultTimeout(Duration.ofSeconds(1));
    }

    @Bean
    public PebbleEngine pebbleEngine(PebbleProperties properties,
                                     Loader<?> pebbleLoader,
                                     SpringExtension springExtension,
                                     @Nullable List<Extension> extensions,
                                     @Nullable MethodAccessValidator methodAccessValidator) {
        PebbleEngine.Builder builder = new PebbleEngine.Builder();
        builder.loader(pebbleLoader);
        builder.extension(springExtension);
        if (extensions != null && !extensions.isEmpty()) {
            builder.extension(extensions.toArray(new Extension[extensions.size()]));
        }
        if (!properties.isCache()) {
            builder.cacheActive(false);
        }
        if (properties.getDefaultLocale() != null) {
            builder.defaultLocale(properties.getDefaultLocale());
        }
        builder.strictVariables(properties.isStrictVariables());
        builder.greedyMatchMethod(properties.isGreedyMatchMethod());
        if (methodAccessValidator != null) {
            builder.methodAccessValidator(methodAccessValidator);
        }
        builder.autoEscaping(false);
        return builder.build();
    }
}