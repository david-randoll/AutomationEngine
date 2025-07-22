package com.davidrandoll.automation.engine.spring.web.events;

import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.spring_web_captor.event.HttpRequestEvent;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class AEHttpRequestEvent extends HttpRequestEvent implements IEvent {
    public AEHttpRequestEvent(HttpRequestEvent event) {
        super(event.toBuilder());
    }
}