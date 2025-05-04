package com.davidrandoll.automation.engine.http.modules.conditions.http_full_path;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.http.event.HttpRequestEvent;
import com.davidrandoll.automation.engine.http.event.HttpResponseEvent;
import com.davidrandoll.automation.engine.http.utils.StringMatcher;
import com.davidrandoll.automation.engine.spi.PluggableCondition;
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
        if (ec.getEvent() instanceof HttpRequestEvent event) {
            return StringMatcher.matchesCondition(cc, event.getFullUrl(), objectMapper);
        } else if (ec.getEvent() instanceof HttpResponseEvent event) {
            return StringMatcher.matchesCondition(cc, event.getFullUrl(), objectMapper);
        }
        return false;
    }
}