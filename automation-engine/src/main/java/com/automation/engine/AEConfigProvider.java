package com.automation.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
@Configuration
@ConfigurationProperties(prefix = "automation.engine")
public class AEConfigProvider {
    private Executor executor;
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private Duration defaultTimeout = Duration.ofSeconds(60);

    @NestedConfigurationProperty
    private TimeBasedProperties timeBased = new TimeBasedProperties();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ConfigurationProperties(prefix = "time-based")
    public static class TimeBasedProperties {
        private boolean enabled = true;
        private String cron = "0 * * * * *"; // every minute
    }
}