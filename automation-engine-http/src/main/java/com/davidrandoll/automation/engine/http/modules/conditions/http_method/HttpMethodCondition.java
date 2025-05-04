package com.davidrandoll.automation.engine.http.modules.conditions.http_method;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.http.event.HttpRequestEvent;
import com.davidrandoll.automation.engine.http.event.HttpResponseEvent;
import com.davidrandoll.automation.engine.http.utils.StringMatcher;
import com.davidrandoll.automation.engine.spi.PluggableCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component("httpMethodCondition")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "httpMethodCondition", ignored = HttpMethodCondition.class)
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