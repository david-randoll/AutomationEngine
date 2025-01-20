package com.automation.engine.templating;

import io.pebbletemplates.pebble.extension.Extension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PebbleConfig {

    @Bean
    public Extension customExtension() {
        return new CustomExtension();
    }
}
