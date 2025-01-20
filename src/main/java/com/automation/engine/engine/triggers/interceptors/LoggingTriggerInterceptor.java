package com.automation.engine.engine.triggers.interceptors;

import com.automation.engine.engine.events.Event;
import com.automation.engine.engine.triggers.ITrigger;
import com.automation.engine.engine.triggers.TriggerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingTriggerInterceptor implements ITriggerInterceptor {
    @Override
    public void intercept(Event event, TriggerContext triggerContext, ITrigger trigger) {
        log.info("Intercepting trigger: {}", trigger.getClass().getSimpleName());
        trigger.isTriggered(event, triggerContext);
        log.info("Trigger {} completed", trigger.getClass().getSimpleName());
    }
}