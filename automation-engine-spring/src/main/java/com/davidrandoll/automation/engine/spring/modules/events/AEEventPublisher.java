package com.davidrandoll.automation.engine.spring.modules.events;

import com.davidrandoll.automation.engine.core.events.publisher.IEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.Assert;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@ConditionalOnMissingBean(value = IEventPublisher.class, ignored = AEEventPublisher.class)
public class AEEventPublisher implements IEventPublisher {
    private final ApplicationEventPublisher publisher;

    @Override
    public void publishEvent(Object event) {
        Assert.notNull(event, "Event cannot be null");
        // We publish events asynchronously to avoid deadlocks when an action blocks the execution thread
        // (e.g. waitForTrigger) and the same thread is used to publish the unblocking event.
        CompletableFuture.runAsync(() -> publisher.publishEvent(event));
    }
}