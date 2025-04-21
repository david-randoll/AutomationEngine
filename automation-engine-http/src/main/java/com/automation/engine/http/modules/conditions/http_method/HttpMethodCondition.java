package com.automation.engine.http.modules.conditions.http_method;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.http.utils.StringMatcher;
import com.automation.engine.spi.PluggableCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("httpMethodCondition")
@RequiredArgsConstructor
public class HttpMethodCondition extends PluggableCondition<HttpMethodContext> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean isSatisfied(EventContext ec, HttpMethodContext cc) {
        if (!(ec.getEvent() instanceof HttpRequestEvent event)) return false;
        return StringMatcher.matchesCondition(cc, event.getMethod(), objectMapper);
    }
}