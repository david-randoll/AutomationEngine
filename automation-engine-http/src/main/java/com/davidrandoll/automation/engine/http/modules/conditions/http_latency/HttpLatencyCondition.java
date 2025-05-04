package com.davidrandoll.automation.engine.http.modules.conditions.http_latency;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.http.event.HttpResponseEvent;
import com.davidrandoll.automation.engine.http.modules.triggers.on_slow_http_request.OnSlowHttpRequestException;
import com.davidrandoll.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component("httpLatencyCondition")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "httpLatencyCondition", ignored = HttpLatencyCondition.class)
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