package com.davidrandoll.automation.engine.http.events;

import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.spring_web_captor.event.HttpResponseEvent;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class AEHttpResponseEvent extends HttpResponseEvent implements IEvent {
    public AEHttpResponseEvent(HttpResponseEvent event) {
        super(event.toBuilder());
    }
}