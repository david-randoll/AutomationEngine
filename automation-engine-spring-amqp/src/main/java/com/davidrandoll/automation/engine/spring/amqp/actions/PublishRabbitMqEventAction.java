package com.davidrandoll.automation.engine.spring.amqp.actions;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PublishRabbitMqEventAction extends PluggableAction<PublishRabbitMqEventActionContext> {
    @Override
    public void doExecute(EventContext ec, PublishRabbitMqEventActionContext ac) {

    }
}
