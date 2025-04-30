package com.automation.engine.modules.events.time_based;

import com.automation.engine.core.AutomationEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "automation.engine.time-based.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class TimeBasedEventPublisher {
    private final AutomationEngine engine;

    @Scheduled(cron = "${automation.engine.time-based.cron}")
    public void run() {
        var timeBasedEvent = new TimeBasedEvent(LocalTime.now());
        engine.publishEvent(timeBasedEvent);
    }
}