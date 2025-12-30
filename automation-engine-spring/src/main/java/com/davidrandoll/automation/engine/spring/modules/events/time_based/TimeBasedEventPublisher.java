package com.davidrandoll.automation.engine.spring.modules.events.time_based;

import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.core.Automation;
import com.davidrandoll.automation.engine.core.events.publisher.AutomationEngineRegisterEvent;
import com.davidrandoll.automation.engine.core.events.publisher.AutomationEngineRemoveAllEvent;
import com.davidrandoll.automation.engine.core.events.publisher.AutomationEngineRemoveEvent;
import com.davidrandoll.automation.engine.spring.AEConfigProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronExpression;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Event-driven time-based event publisher that schedules automations
 * only when they are registered, rather than polling on a fixed schedule.
 *
 * <p>
 * When an automation with a time-based trigger is registered, this publisher:
 * <ol>
 * <li>Publishes an immediate TimeBasedEvent to trigger initial evaluation</li>
 * <li>Allows the trigger to schedule future events by calling scheduleAt()</li>
 * </ol>
 */
@Slf4j
@RequiredArgsConstructor
public class TimeBasedEventPublisher implements DisposableBean {
    private final AutomationEngine engine;
    private final AEConfigProvider configProvider;

    // Map to track scheduled futures by schedule key (combination of automation
    // alias and time)
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /**
     * Listens for automation registration events and publishes an immediate event
     * to allow time-based triggers to schedule themselves.
     */
    @EventListener
    public void onAutomationRegistered(AutomationEngineRegisterEvent event) {
        Automation automation = event.getAutomation();
        log.debug("Automation registered: '{}'. Publishing immediate TimeBasedEvent for trigger evaluation.",
                automation.getAlias());

        // Publish an immediate event so time-based triggers can evaluate and schedule
        // themselves
        publishEvent(LocalDateTime.now());
    }

    /**
     * Listens for automation removal events and cancels any scheduled tasks for
     * that automation.
     */
    @EventListener
    public void onAutomationRemoved(AutomationEngineRemoveEvent event) {
        Automation automation = event.getAutomation();
        String automationAlias = automation.getAlias();

        // Cancel all scheduled tasks for this automation
        scheduledTasks.keySet().stream()
                .filter(key -> key.startsWith(automationAlias + ":"))
                .forEach(this::cancelScheduledTask);
    }

    /**
     * Listens for remove all automations event and cancels all scheduled tasks.
     */
    @EventListener
    public void onAllAutomationsRemoved(AutomationEngineRemoveAllEvent event) {
        log.debug("Cancelling all {} scheduled tasks", scheduledTasks.size());
        scheduledTasks.values().forEach(future -> future.cancel(false));
        scheduledTasks.clear();
    }

    /**
     * Schedules a time-based event to be published at the specified time.
     * This method is called by TimeBasedTrigger when it determines it needs to be
     * scheduled.
     *
     * @param automationAlias The alias of the automation to schedule
     * @param targetTime      The time at which to publish the event
     */
    public void scheduleAt(String automationAlias, LocalTime targetTime) {
        String scheduleKey = automationAlias + ":" + targetTime;

        // if existing schedule is already set for this time, skip scheduling
        ScheduledFuture<?> existingFuture = scheduledTasks.get(scheduleKey);
        if (existingFuture != null && !existingFuture.isDone()) {
            log.debug("Schedule already exists for automation '{}' at {}, skipping scheduling",
                    automationAlias, targetTime);
            return;
        }

        ThreadPoolTaskScheduler taskScheduler = configProvider.getTaskScheduler();

        // Calculate the next occurrence of the target time
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetDateTime = now.toLocalDate().atTime(targetTime);

        // If the time has already passed today, schedule for tomorrow
        if (targetDateTime.isBefore(now) || targetDateTime.isEqual(now)) {
            targetDateTime = targetDateTime.plusDays(1);
        }

        Date scheduledDate = java.sql.Timestamp.valueOf(targetDateTime);

        LocalDateTime finalTargetDateTime = targetDateTime;
        ScheduledFuture<?> future = taskScheduler.schedule(() -> {
            log.debug("Scheduled time reached for automation '{}' at {}", automationAlias, targetTime);
            publishEvent(finalTargetDateTime);
        }, scheduledDate);

        scheduledTasks.put(scheduleKey, future);

        Duration timeUntil = Duration.between(now, targetDateTime);
        log.debug("Scheduled automation '{}' to trigger at {} (in {})",
                automationAlias, targetTime, formatDuration(timeUntil));
    }

    /**
     * Schedules a time-based event to be published based on a cron expression.
     *
     * @param automationAlias The alias of the automation to schedule
     * @param cronExpression  The cron expression to use
     */
    public void scheduleCron(String automationAlias, String cronExpression) {
        String scheduleKey = automationAlias + ":cron:" + cronExpression;

        // if existing schedule is already set for this cron, skip scheduling
        ScheduledFuture<?> existingFuture = scheduledTasks.get(scheduleKey);
        if (existingFuture != null && !existingFuture.isDone()) {
            return;
        }

        ThreadPoolTaskScheduler taskScheduler = configProvider.getTaskScheduler();

        CronExpression cron = CronExpression.parse(cronExpression);
        LocalDateTime next = cron.next(LocalDateTime.now());

        if (next == null) {
            log.warn("Cron expression '{}' will never fire again.", cronExpression);
            return;
        }

        Date scheduledDate = java.sql.Timestamp.valueOf(next);

        ScheduledFuture<?> future = taskScheduler.schedule(() -> {
            log.debug("Cron trigger reached for automation '{}' with expression '{}'", automationAlias, cronExpression);
            publishEvent(next);
        }, scheduledDate);

        scheduledTasks.put(scheduleKey, future);

        Duration timeUntil = Duration.between(LocalDateTime.now(), next);
        log.debug("Scheduled automation '{}' with cron '{}' to trigger at {} (in {})",
                automationAlias, cronExpression, next, formatDuration(timeUntil));
    }

    /**
     * Cancels a scheduled task by key.
     */
    private void cancelScheduledTask(String scheduleKey) {
        ScheduledFuture<?> future = scheduledTasks.remove(scheduleKey);
        if (future != null) {
            future.cancel(false);
            log.debug("Cancelled scheduled task: {}", scheduleKey);
        }
    }

    /**
     * Publishes a TimeBasedEvent with the specified time.
     */
    private void publishEvent(LocalDateTime dateTime) {
        try {
            var timeBasedEvent = new TimeBasedEvent(dateTime.toLocalTime(), dateTime);
            engine.publishEvent(timeBasedEvent);
            log.trace("Published TimeBasedEvent at {}", dateTime);
        } catch (Exception e) {
            log.error("Failed to publish time-based event", e);
        }
    }

    /**
     * Formats a duration for logging.
     */
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%dh %dm %ds", hours, minutes, seconds);
    }

    @Override
    public void destroy() {
        log.debug("Shutting down TimeBasedEventPublisher, cancelling {} scheduled tasks",
                scheduledTasks.size());
        scheduledTasks.values().forEach(future -> future.cancel(true));
        scheduledTasks.clear();
    }
}
