package com.davidrandoll.automation.engine.backend.api;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "automation-engine.backend-api")
public class AutomationEngineBackendApiProperties {
    /**
     * The path where the Automation Engine Backend API will be served.
     * Default is /automation-engine
     */
    private String path = "/automation-engine";
}
