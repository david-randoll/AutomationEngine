package com.davidrandoll.automation.engine.spring.web.modules.conditions.http_full_path;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.web.utils.StringMatcher;
import com.davidrandoll.automation.engine.spi.PluggableCondition;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpRequestEvent;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component("httpFullPathCondition")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "httpFullPathCondition", ignored = HttpFullPathCondition.class)
public class HttpFullPathCondition extends PluggableCondition<HttpFullPathConditionContext> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean isSatisfied(EventContext ec, HttpFullPathConditionContext cc) {
        if (ec.getEvent() instanceof AEHttpRequestEvent event) {
            return StringMatcher.matchesCondition(cc, event.getFullUrl(), objectMapper);
        } else if (ec.getEvent() instanceof AEHttpResponseEvent event) {
            return StringMatcher.matchesCondition(cc, event.getFullUrl(), objectMapper);
        }
        return false;
    }
}