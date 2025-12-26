package com.davidrandoll.automation.engine.templating;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "automation-engine.templating")
public class AETemplatingProperties {

    /**
     * Default templating engine to use when not specified in the automation block.
     * Defaults to "pebble".
     */
    private String defaultEngine = "pebble";
}
