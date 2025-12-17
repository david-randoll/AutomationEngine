package com.davidrandoll.automation.engine.ui;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "automation-engine.ui")
public class AutomationEngineUiProperties {
    /**
     * Enable or disable the Automation Engine UI.
     */
    private boolean enabled = true;

    /**
     * The path where the Automation Engine UI will be served.
     * Default is /automation-engine-ui
     */
    private String path = "/automation-engine-ui";
}
