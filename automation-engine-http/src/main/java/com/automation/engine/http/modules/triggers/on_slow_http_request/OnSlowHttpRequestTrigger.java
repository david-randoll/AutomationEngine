package com.automation.engine.http.modules.triggers.on_slow_http_request;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpResponseEvent;
import com.automation.engine.spi.PluggableTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component("onSlowHttpRequestTrigger")
@RequiredArgsConstructor
public class OnSlowHttpRequestTrigger extends PluggableTrigger<OnSlowHttpRequestContext> {
    @Override
    public boolean isTriggered(EventContext ec, OnSlowHttpRequestContext tc) {
        if (!(ec.getEvent() instanceof HttpResponseEvent event)) return false;
        Duration eventDuration = event.getAdditionalData("duration", Duration.class);
        if (eventDuration == null)
            throw new OnSlowHttpRequestException("Duration not found in event");
        Duration tcDuration = tc.getDuration();
        if (tcDuration == null)
            throw new OnSlowHttpRequestException("Duration not found in trigger context");
        return eventDuration.compareTo(tcDuration) > 0;
    }
}