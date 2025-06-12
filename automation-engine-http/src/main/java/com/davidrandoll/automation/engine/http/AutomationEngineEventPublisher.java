package com.davidrandoll.automation.engine.http;

import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.core.events.IEvent;
import com.davidrandoll.automation.engine.http.events.AEHttpRequestEvent;
import com.davidrandoll.automation.engine.http.events.AEHttpResponseEvent;
import com.davidrandoll.spring_web_captor.event.HttpRequestEvent;
import com.davidrandoll.spring_web_captor.event.HttpResponseEvent;
import com.davidrandoll.spring_web_captor.publisher.IWebCaptorEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutomationEngineEventPublisher implements IWebCaptorEventPublisher {
    private final AutomationEngine publisher;

    @Override
    public void publishEvent(Object event) {
        IEvent automationEvent = null;
        if (event instanceof HttpRequestEvent requestEvent) {
            automationEvent = new AEHttpRequestEvent(requestEvent);
        } else if (event instanceof HttpResponseEvent responseEvent) {
            automationEvent = new AEHttpResponseEvent(responseEvent);
        }
        if (automationEvent == null) {
            log.warn("AutomationEngineEventPublisher.publishEvent() - Unsupported event type: {}", event.getClass().getName());
            return;
        }
        publisher.publishEvent(automationEvent);
    }
}