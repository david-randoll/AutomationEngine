package com.automation.engine.http.modules.conditions.http_latency;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpResponseEvent;
import com.automation.engine.http.modules.triggers.on_slow_http_request.OnSlowHttpRequestException;
import com.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component("httpLatencyCondition")
@RequiredArgsConstructor
public class HttpLatencyCondition extends PluggableCondition<HttpLatencyConditionContext> {
    @Override
    public boolean isSatisfied(EventContext ec, HttpLatencyConditionContext cc) {
        if (!(ec.getEvent() instanceof HttpResponseEvent event)) return false;
        Duration eventDuration = event.getAdditionalData("duration", Duration.class);
        if (eventDuration == null)
            throw new OnSlowHttpRequestException("Duration not found in event");
        Duration tcDuration = cc.getDuration();
        if (tcDuration == null)
            throw new OnSlowHttpRequestException("Duration not found in trigger context");
        return eventDuration.compareTo(tcDuration) > 0;
    }
}