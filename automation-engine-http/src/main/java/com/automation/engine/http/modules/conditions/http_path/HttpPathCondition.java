package com.automation.engine.http.modules.conditions.http_path;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.http.utils.StringMatcher;
import com.automation.engine.spi.PluggableCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("httpPathCondition")
@RequiredArgsConstructor
public class HttpPathCondition extends PluggableCondition<HttpPathContext> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean isSatisfied(EventContext ec, HttpPathContext cc) {
        if (!(ec.getEvent() instanceof HttpRequestEvent event)) return false;
        return StringMatcher.matchesCondition(cc, event.getPath(), objectMapper);
    }
}