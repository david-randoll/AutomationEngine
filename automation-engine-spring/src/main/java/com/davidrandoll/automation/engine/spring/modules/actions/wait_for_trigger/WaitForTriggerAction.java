package com.davidrandoll.automation.engine.spring.modules.actions.wait_for_trigger;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.spring.AEConfigProvider;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.event.ApplicationEventMulticaster;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class WaitForTriggerAction extends PluggableAction<WaitForTriggerActionContext> {
    private final ApplicationEventMulticaster multicaster;
    private final AEConfigProvider configProvider;

    @Override
    public void doExecute(EventContext ec, WaitForTriggerActionContext ac) {
        if (ac.getTriggers() == null || ac.getTriggers().isEmpty()) {
            log.debug("No triggers specified for waitForTrigger action. Continuing immediately.");
            return;
        }

        java.time.Duration timeout = ac.getTimeout();
        if (timeout == null && configProvider != null) {
            timeout = configProvider.getDefaultTimeout();
        }
        if (timeout == null) {
            timeout = java.time.Duration.ofSeconds(30);
        }

        log.debug("Waiting for triggers: {} with timeout {}", ac.getTriggers(), timeout);

        // Check if any trigger matches immediately (also allows triggers to schedule themselves)
        if (processor.anyTriggersTriggered(ec, ac.getTriggers())) {
            log.debug("Trigger matched immediately in waitForTrigger action.");
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);

        ApplicationListener<ApplicationEvent> listener = event -> {
            Object payload = event;
            if (event instanceof PayloadApplicationEvent<?> pae) {
                payload = pae.getPayload();
            }

            EventContext newEc = switch (payload) {
                case EventContext ec1 -> ec1;
                case IEvent e1 -> EventContext.of(e1);
                default -> null;
            };

            if (newEc == null) return;

            if (processor.anyTriggersTriggered(newEc, ac.getTriggers())) {
                log.debug("Trigger matched in waitForTrigger action: {}", newEc.getEvent());
                latch.countDown();
            }
        };

        multicaster.addApplicationListener(listener);
        try {
            boolean triggered = latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (triggered) {
                log.debug("WaitForTriggerAction: Trigger received before timeout.");
            } else {
                log.debug("WaitForTriggerAction: Timeout reached.");
            }
        } catch (InterruptedException e) {
            log.error("WaitForTriggerAction interrupted", e);
            Thread.currentThread().interrupt();
        } finally {
            multicaster.removeApplicationListener(listener);
        }
    }
}