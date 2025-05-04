package com.davidrandoll.automation.engine.http.modules.conditions.http_response_body;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.http.event.HttpResponseEvent;
import com.davidrandoll.automation.engine.http.utils.StringMatcher;
import com.davidrandoll.automation.engine.spi.PluggableCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component("httpResponseBodyCondition")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "httpResponseBodyCondition", ignored = HttpResponseBodyCondition.class)
public class HttpResponseBodyCondition extends PluggableCondition<HttpResponseBodyConditionContext> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean isSatisfied(EventContext ec, HttpResponseBodyConditionContext cc) {
        if (!(ec.getEvent() instanceof HttpResponseEvent event)) return false;
        return StringMatcher.matchesCondition(cc.getResponseBody(), event.getResponseBody(), objectMapper);
    }
}