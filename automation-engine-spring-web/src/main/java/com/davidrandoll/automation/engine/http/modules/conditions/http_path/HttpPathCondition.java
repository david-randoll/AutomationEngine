package com.davidrandoll.automation.engine.http.modules.conditions.http_path;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.http.events.AEHttpRequestEvent;
import com.davidrandoll.automation.engine.http.events.AEHttpResponseEvent;
import com.davidrandoll.automation.engine.http.utils.StringMatcher;
import com.davidrandoll.automation.engine.spi.PluggableCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component("httpPathCondition")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "httpPathCondition", ignored = HttpPathCondition.class)
public class HttpPathCondition extends PluggableCondition<HttpPathConditionContext> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean isSatisfied(EventContext ec, HttpPathConditionContext cc) {
        if (ec.getEvent() instanceof AEHttpRequestEvent event) {
            return StringMatcher.matchesCondition(cc, event.getPath(), objectMapper);
        } else if (ec.getEvent() instanceof AEHttpResponseEvent event) {
            return StringMatcher.matchesCondition(cc, event.getPath(), objectMapper);
        }
        return false;
    }
}