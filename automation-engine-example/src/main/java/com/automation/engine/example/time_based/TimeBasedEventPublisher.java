package com.automation.engine.example.time_based;

import com.automation.engine.core.AutomationEngine;
import com.automation.engine.modules.time_based.TimeBasedEvent;
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

    @Scheduled(fixedRate = 10000)
    public void run() {
        engine.processEvent(new TimeBasedEvent(LocalTime.now()));
    }
}
