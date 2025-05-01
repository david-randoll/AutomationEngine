package com.automation.engine.http.modules.conditions.http_header;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.http.event.HttpResponseEvent;
import com.automation.engine.http.utils.StringMatcher;
import com.automation.engine.spi.PluggableCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component("httpHeaderCondition")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "httpHeaderCondition", ignored = HttpHeaderCondition.class)
public class HttpHeaderCondition extends PluggableCondition<HttpHeaderConditionContext> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean isSatisfied(EventContext ec, HttpHeaderConditionContext cc) {
        if (ec.getEvent() instanceof HttpRequestEvent event) {
            return StringMatcher.matchesCondition(cc.getHeaders(), event.getHeaders(), objectMapper);
        } else if (ec.getEvent() instanceof HttpResponseEvent event) {
            return StringMatcher.matchesCondition(cc.getHeaders(), event.getHeaders(), objectMapper);
        }
        return false;
    }
}