package com.automation.engine.http.modules.conditions.http_method;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("httpMethodCondition")
@RequiredArgsConstructor
public class HttpMethodCondition extends PluggableCondition<HttpMethodContext> {
    @Override
    public boolean isSatisfied(EventContext ec, HttpMethodContext cc) {
        if (!(ec.getEvent() instanceof HttpRequestEvent event)) return false;
        return cc.matches(event.getMethod().getMethod());
    }
}