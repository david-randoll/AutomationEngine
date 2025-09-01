package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_latency;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableCondition;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpResponseEvent;
import com.davidrandoll.automation.engine.spring.web.modules.triggers.on_slow_http_request.OnSlowHttpRequestException;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@RequiredArgsConstructor
public class HttpLatencyCondition extends PluggableCondition<HttpLatencyConditionContext> {
    @Override
    public boolean isSatisfied(EventContext ec, HttpLatencyConditionContext cc) {
        if (!(ec.getEvent() instanceof AEHttpResponseEvent event)) return false;
        Duration eventDuration = event.getAdditionalData("duration", Duration.class);
        if (eventDuration == null)
            throw new OnSlowHttpRequestException("Duration not found in event");
        Duration tcDuration = cc.getDuration();
        if (tcDuration == null)
            throw new OnSlowHttpRequestException("Duration not found in trigger context");
        return eventDuration.compareTo(tcDuration) > 0;
    }
}