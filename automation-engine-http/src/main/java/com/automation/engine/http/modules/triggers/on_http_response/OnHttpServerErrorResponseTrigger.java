package com.automation.engine.http.modules.triggers.on_http_response;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpResponseEvent;
import com.automation.engine.spi.PluggableTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("onHttpServerErrorResponseTrigger")
@RequiredArgsConstructor
public class OnHttpServerErrorResponseTrigger extends PluggableTrigger<OnHttpResponseTriggerContext> {
    private final OnHttpResponseTrigger onHttpResponseTrigger;

    @Override
    public boolean isTriggered(EventContext ec, OnHttpResponseTriggerContext tc) {
        if (!(ec.getEvent() instanceof HttpResponseEvent event)) return false;
        if (!event.isServerErrorResponse()) return false;

        return onHttpResponseTrigger.isTriggered(ec, tc);
    }
}