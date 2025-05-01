package com.automation.engine.modules.events;

import com.automation.engine.core.events.publisher.IEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(value = IEventPublisher.class, ignored = AEEventPublisher.class)
public class AEEventPublisher implements IEventPublisher {
    private final ApplicationEventPublisher publisher;

    @Override
    public void publishEvent(Object event) {
        Assert.notNull(event, "Event cannot be null");
        publisher.publishEvent(event);
    }
}