package com.davidrandoll.automation.engine.spring.events.modules.publish_spring_event;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableAction;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component("publishSpringEventAction")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "publishSpringEventAction", ignored = PublishSpringEventAction.class)
public class PublishSpringEventAction extends PluggableAction<PublishSpringEventActionContext> {
    private final ApplicationEventPublisher publisher;
    private final ObjectMapper mapper;

    @Override
    public void doExecute(EventContext ec, PublishSpringEventActionContext ac) {
        if (ac.getClassName() != null) {
            try {
                Class<?> eventClass = Class.forName(ac.getClassName());
                Object event = mapper.convertValue(ac.getData(), eventClass);
                publisher.publishEvent(event);
                log.info("Published Spring event of type: {}", ac.getClassName());
            } catch (ClassNotFoundException e) {
                log.error("Failed to publish Spring event, class not found: {}", ac.getClassName(), e);
            }
        } else {
            publisher.publishEvent(new PublishSpringEvent(ac.getData()));
        }
    }
}