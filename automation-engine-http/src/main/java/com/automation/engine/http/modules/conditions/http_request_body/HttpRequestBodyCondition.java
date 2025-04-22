package com.automation.engine.http.modules.conditions.http_request_body;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.http.event.HttpResponseEvent;
import com.automation.engine.http.utils.StringMatcher;
import com.automation.engine.spi.PluggableCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("httpRequestBodyCondition")
@RequiredArgsConstructor
public class HttpRequestBodyCondition extends PluggableCondition<HttpRequestBodyConditionContext> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean isSatisfied(EventContext ec, HttpRequestBodyConditionContext cc) {
        if (ec.getEvent() instanceof HttpRequestEvent event) {
            return StringMatcher.matchesCondition(cc.getRequestBody(), event.getRequestBody(), objectMapper);
        } else if (ec.getEvent() instanceof HttpResponseEvent event) {
            return StringMatcher.matchesCondition(cc.getRequestBody(), event.getRequestBody(), objectMapper);
        }
        return false;
    }
}