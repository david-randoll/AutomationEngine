package com.automation.engine.modules.events.time_based;

import com.automation.engine.config.AEConfigProvider;
import com.automation.engine.core.AutomationEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.support.CronTrigger;

import java.time.LocalTime;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TimeBasedEventPublisher implements InitializingBean, DisposableBean {
    private final AutomationEngine engine;
    private final AEConfigProvider configProvider;

    private ScheduledFuture<?> scheduledTask;

    @Override
    public void afterPropertiesSet() {
        var cronExpression = configProvider.getTimeBased().getCron();
        var taskScheduler = configProvider.getTaskScheduler();

        scheduledTask = taskScheduler.schedule(this::publishEvent, new CronTrigger(cronExpression));
    }

    private void publishEvent() {
        try {
            var timeBasedEvent = new TimeBasedEvent(LocalTime.now());
            engine.publishEvent(timeBasedEvent);
        } catch (Exception e) {
            log.error("Failed to publish time-based event", e);
        }
    }

    @Override
    public void destroy() {
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
        }
    }
}
