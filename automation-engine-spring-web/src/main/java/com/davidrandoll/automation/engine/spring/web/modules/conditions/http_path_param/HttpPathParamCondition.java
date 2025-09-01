package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_path_param;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableCondition;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpRequestEvent;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpResponseEvent;
import com.davidrandoll.automation.engine.spring.web.utils.StringMatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HttpPathParamCondition extends PluggableCondition<HttpPathParamConditionContext> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean isSatisfied(EventContext ec, HttpPathParamConditionContext cc) {
        if (ec.getEvent() instanceof AEHttpRequestEvent event) {
            return StringMatcher.matchesCondition(cc.getPathParams(), event.getPathParams(), objectMapper);
        } else if (ec.getEvent() instanceof AEHttpResponseEvent event) {
            return StringMatcher.matchesCondition(cc.getPathParams(), event.getPathParams(), objectMapper);
        }
        return false;
    }
}