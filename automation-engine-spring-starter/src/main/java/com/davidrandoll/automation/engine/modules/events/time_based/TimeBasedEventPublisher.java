package com.davidrandoll.automation.engine.modules.events.time_based;

import com.davidrandoll.automation.engine.core.AutomationEngine;
import com.davidrandoll.automation.engine.provider.AEConfigProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component("timeBasedEventPublisher")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "timeBasedEventPublisher", ignored = TimeBasedEventPublisher.class)
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
