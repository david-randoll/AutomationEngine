package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_header;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpRequestEvent;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpResponseEvent;
import com.davidrandoll.automation.engine.spring.web.utils.StringMatcher;
import com.davidrandoll.automation.engine.spi.PluggableCondition;
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
        if (ec.getEvent() instanceof AEHttpRequestEvent event) {
            return StringMatcher.matchesCondition(cc.getHeaders(), event.getHeaders(), objectMapper);
        } else if (ec.getEvent() instanceof AEHttpResponseEvent event) {
            return StringMatcher.matchesCondition(cc.getHeaders(), event.getHeaders(), objectMapper);
        }
        return false;
    }
}