package com.automation.engine.templating.interceptors;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.core.triggers.ITrigger;
import com.automation.engine.core.triggers.TriggerContext;
import com.automation.engine.core.triggers.interceptors.ITriggerInterceptor;
import com.automation.engine.templating.TemplateProcessor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(-1)
public class TriggerTemplatingInterceptor implements ITriggerInterceptor {
    private final TemplateProcessor templateProcessor;

    @Override
    @SneakyThrows
    public void intercept(EventContext eventContext, TriggerContext context, ITrigger trigger) {
        log.debug("TriggerTemplatingInterceptor: Processing trigger data...");
        if (ObjectUtils.isEmpty(context.getData()) || ObjectUtils.isEmpty(eventContext.getEventData())) {
            trigger.isTriggered(eventContext, context);
        }

        var mapCopy = new HashMap<>(context.getData());
        for (Map.Entry<String, Object> entry : mapCopy.entrySet()) {
            if (entry.getValue() instanceof String valueStr) {
                String processedValue = templateProcessor.process(valueStr, eventContext.getEventData());
                entry.setValue(processedValue);
            }
        }
        trigger.isTriggered(eventContext, new TriggerContext(mapCopy));
        log.debug("TriggerTemplatingInterceptor: Trigger data processed successfully.");
    }
}