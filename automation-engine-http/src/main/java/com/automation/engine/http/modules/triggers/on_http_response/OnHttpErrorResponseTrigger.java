package com.automation.engine.http.modules.triggers.on_http_response;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpResponseEvent;
import com.automation.engine.spi.PluggableTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component("onHttpErrorResponseTrigger")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "onHttpErrorResponseTrigger", ignored = OnHttpErrorResponseTrigger.class)
public class OnHttpErrorResponseTrigger extends PluggableTrigger<OnHttpResponseTriggerContext> {
    private final OnHttpResponseTrigger onHttpResponseTrigger;

    @Override
    public boolean isTriggered(EventContext ec, OnHttpResponseTriggerContext tc) {
        if (!(ec.getEvent() instanceof HttpResponseEvent event)) return false;
        if (!event.isErrorResponse()) return false;

        return onHttpResponseTrigger.isTriggered(ec, tc);
    }
}