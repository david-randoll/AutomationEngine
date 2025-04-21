package com.automation.engine.http.modules.conditions.http_full_path;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.http.utils.StringMatcher;
import com.automation.engine.spi.PluggableCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("httpFullPathCondition")
@RequiredArgsConstructor
public class HttpFullPathCondition extends PluggableCondition<HttpFullPathContext> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean isSatisfied(EventContext ec, HttpFullPathContext cc) {
        if (!(ec.getEvent() instanceof HttpRequestEvent event)) return false;
        return StringMatcher.matchesCondition(cc, event.getFullUrl(), objectMapper);
    }
}