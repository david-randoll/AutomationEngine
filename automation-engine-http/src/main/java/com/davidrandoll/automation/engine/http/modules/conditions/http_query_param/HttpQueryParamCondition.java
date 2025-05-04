package com.davidrandoll.automation.engine.http.modules.conditions.http_query_param;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.http.event.HttpRequestEvent;
import com.davidrandoll.automation.engine.http.event.HttpResponseEvent;
import com.davidrandoll.automation.engine.http.utils.StringMatcher;
import com.davidrandoll.automation.engine.spi.PluggableCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component("httpQueryParamCondition")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "httpQueryParamCondition", ignored = HttpQueryParamCondition.class)
public class HttpQueryParamCondition extends PluggableCondition<HttpQueryParamConditionContext> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean isSatisfied(EventContext ec, HttpQueryParamConditionContext cc) {
        if (ec.getEvent() instanceof HttpRequestEvent event) {
            return StringMatcher.matchesCondition(cc.getQueryParams(), event.getQueryParams(), objectMapper);
        } else if (ec.getEvent() instanceof HttpResponseEvent event) {
            return StringMatcher.matchesCondition(cc.getQueryParams(), event.getQueryParams(), objectMapper);
        }
        return false;
    }
}