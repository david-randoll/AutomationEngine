package com.davidrandoll.automation.engine.spring.events.modules.publish_spring_event;

import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import com.davidrandoll.automation.engine.spring.utils.AutomationProxyUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@RequiredArgsConstructor
public class PublishSpringEventAction extends PluggableAction<PublishSpringEventActionContext> {
    private final AutomationEngine engine;
    private final ApplicationEventPublisher publisher;
    private final ObjectMapper mapper;

    @Override
    public void doExecute(EventContext ec, PublishSpringEventActionContext ac) {
        Object event;
        if (ac.getClassName() != null) {
            try {
                Class<?> eventClass = Class.forName(ac.getClassName());
                event = mapper.convertValue(ac.getData(), eventClass);
            } catch (ClassNotFoundException e) {
                throw new AEClassNotFoundException("Failed to publish Spring event, class not found: " + ac.getClassName(), e);
            }
        } else {
            event = new PublishSpringEvent(ac.getData());
        }

        var eventProxy = AutomationProxyUtil.markWithAutomationOriginOrThrow(event, AutomationOrigin.class);

        publisher.publishEvent(eventProxy);
        log.info("Published Spring event of type: {}", eventProxy.getClass().getName());

        if (ac.isPublishToAutomationEngine()) {
            var iEvent = engine.getEventFactory().createEvent(event);
            engine.publishEvent(iEvent);
        }
    }
}