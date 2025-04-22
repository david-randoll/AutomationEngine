package com.automation.engine.http.modules.conditions.http_method;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.http.event.HttpResponseEvent;
import com.automation.engine.http.utils.StringMatcher;
import com.automation.engine.spi.PluggableCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("httpMethodCondition")
@RequiredArgsConstructor
public class HttpMethodCondition extends PluggableCondition<HttpMethodConditionContext> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean isSatisfied(EventContext ec, HttpMethodConditionContext cc) {
        if (ec.getEvent() instanceof HttpRequestEvent event) {
            return StringMatcher.matchesCondition(cc, event.getMethod(), objectMapper);
        } else if (ec.getEvent() instanceof HttpResponseEvent event) {
            return StringMatcher.matchesCondition(cc, event.getMethod(), objectMapper);
        }
        return false;
    }
}