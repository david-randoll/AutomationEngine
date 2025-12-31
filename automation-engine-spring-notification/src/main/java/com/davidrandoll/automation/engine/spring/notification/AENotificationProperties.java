package com.davidrandoll.automation.engine.spring.notification;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Automation Engine Notification module.
 * <p>
 * These properties allow configuring default email settings that will be used
 * when specific values are not provided in the action context.
 * </p>
 *
 * <p>Example configuration in application.yml:</p>
 * <pre>
 * automation-engine:
 *   notification:
 *     default-from: "noreply@example.com"
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "automation-engine.notification")
public class AENotificationProperties {

    /**
     * Default 'from' email address to use when not specified in the action.
     */
    private String defaultFrom;
}
