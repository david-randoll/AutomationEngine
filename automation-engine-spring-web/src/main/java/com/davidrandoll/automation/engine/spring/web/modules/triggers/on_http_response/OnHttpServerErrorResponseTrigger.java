package com.davidrandoll.automation.engine.spring.web.modules.triggers.on_http_response;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableTrigger;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpResponseEvent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OnHttpServerErrorResponseTrigger extends PluggableTrigger<OnHttpResponseTriggerContext> {
    private final OnHttpResponseTrigger onHttpResponseTrigger;

    @Override
    public boolean isTriggered(EventContext ec, OnHttpResponseTriggerContext tc) {
        if (!(ec.getEvent() instanceof AEHttpResponseEvent event)) return false;
        if (!event.isServerErrorResponse()) return false;

        return onHttpResponseTrigger.isTriggered(ec, tc);
    }
}