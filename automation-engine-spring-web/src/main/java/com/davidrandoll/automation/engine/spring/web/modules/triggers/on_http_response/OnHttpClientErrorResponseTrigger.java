package com.davidrandoll.automation.engine.spring.web.modules.triggers.on_http_response;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpResponseEvent;
import com.davidrandoll.automation.engine.spi.PluggableTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component("onHttpClientErrorResponseTrigger")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "onHttpClientErrorResponseTrigger", ignored = OnHttpClientErrorResponseTrigger.class)
public class OnHttpClientErrorResponseTrigger extends PluggableTrigger<OnHttpResponseTriggerContext> {
    private final OnHttpResponseTrigger onHttpResponseTrigger;

    @Override
    public boolean isTriggered(EventContext ec, OnHttpResponseTriggerContext tc) {
        if (!(ec.getEvent() instanceof AEHttpResponseEvent event)) return false;
        if (!event.isClientErrorResponse()) return false;

        return onHttpResponseTrigger.isTriggered(ec, tc);
    }
}