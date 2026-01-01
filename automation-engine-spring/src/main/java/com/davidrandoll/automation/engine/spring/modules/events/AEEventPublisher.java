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
        // We publish events synchronously to ensure tests pass and avoid race conditions.
        // If deadlocks occur in waitForTrigger, we may need to reconsider this or use a different approach for tests.
        publisher.publishEvent(event);
    }
}