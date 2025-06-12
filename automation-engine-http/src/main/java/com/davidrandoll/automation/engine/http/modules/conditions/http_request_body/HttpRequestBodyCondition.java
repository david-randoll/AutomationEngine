package com.davidrandoll.automation.engine.http.modules.conditions.http_request_body;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.http.events.AEHttpRequestEvent;
import com.davidrandoll.automation.engine.http.events.AEHttpResponseEvent;
import com.davidrandoll.automation.engine.http.utils.StringMatcher;
import com.davidrandoll.automation.engine.spi.PluggableCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component("httpRequestBodyCondition")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "httpRequestBodyCondition", ignored = HttpRequestBodyCondition.class)
public class HttpRequestBodyCondition extends PluggableCondition<HttpRequestBodyConditionContext> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean isSatisfied(EventContext ec, HttpRequestBodyConditionContext cc) {
        if (ec.getEvent() instanceof AEHttpRequestEvent event) {
            return StringMatcher.matchesCondition(cc.getRequestBody(), event.getRequestBody(), objectMapper);
        } else if (ec.getEvent() instanceof AEHttpResponseEvent event) {
            return StringMatcher.matchesCondition(cc.getRequestBody(), event.getRequestBody(), objectMapper);
        }
        return false;
    }
}