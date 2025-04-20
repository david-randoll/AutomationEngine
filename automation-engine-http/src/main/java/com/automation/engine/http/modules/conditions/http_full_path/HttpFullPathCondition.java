package com.automation.engine.http.modules.conditions.http_full_path;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("httpFullPathCondition")
@RequiredArgsConstructor
public class HttpFullPathCondition extends PluggableCondition<HttpFullPathContext> {
    @Override
    public boolean isSatisfied(EventContext ec, HttpFullPathContext cc) {
        if (!(ec.getEvent() instanceof HttpRequestEvent event)) return false;
        return cc.matches(event.getFullUrl());
    }
}