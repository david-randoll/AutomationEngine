package com.automation.engine.modules.events.time_based;

import com.automation.engine.core.AutomationEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeBasedEventPublisher {
    private final AutomationEngine engine;

    // publish a TimeBasedEvent every minute (60 seconds)
    @Scheduled(fixedRate = 60_000)
    public void run() {
        var timeBasedEvent = new TimeBasedEvent(LocalTime.now());
        engine.publishEvent(timeBasedEvent);
    }
}