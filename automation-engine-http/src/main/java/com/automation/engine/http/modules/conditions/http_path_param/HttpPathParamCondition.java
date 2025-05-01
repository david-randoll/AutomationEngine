package com.automation.engine.http.modules.conditions.http_path_param;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.http.event.HttpResponseEvent;
import com.automation.engine.http.utils.StringMatcher;
import com.automation.engine.spi.PluggableCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component("httpPathParamCondition")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "httpPathParamCondition", ignored = HttpPathParamCondition.class)
public class HttpPathParamCondition extends PluggableCondition<HttpPathParamConditionContext> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean isSatisfied(EventContext ec, HttpPathParamConditionContext cc) {
        if (ec.getEvent() instanceof HttpRequestEvent event) {
            return StringMatcher.matchesCondition(cc.getPathParams(), event.getPathParams(), objectMapper);
        } else if (ec.getEvent() instanceof HttpResponseEvent event) {
            return StringMatcher.matchesCondition(cc.getPathParams(), event.getPathParams(), objectMapper);
        }
        return false;
    }
}