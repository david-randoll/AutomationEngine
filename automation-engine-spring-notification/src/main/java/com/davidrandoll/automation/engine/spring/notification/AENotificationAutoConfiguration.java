package com.davidrandoll.automation.engine.spring.notification;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(AENotificationConfig.class)
public class AENotificationAutoConfiguration {
}
