package com.automation.engine.templating.interceptors;

import com.automation.engine.core.conditions.ConditionContext;
import com.automation.engine.core.conditions.ICondition;
import com.automation.engine.core.conditions.interceptors.IConditionInterceptor;
import com.automation.engine.core.events.EventContext;
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
public class ConditionTemplatingInterceptor implements IConditionInterceptor {
    private final TemplateProcessor templateProcessor;

    @Override
    @SneakyThrows
    public void intercept(EventContext eventContext, ConditionContext conditionContext, ICondition condition) {
        log.debug("ConditionTemplatingInterceptor: Processing condition data...");
        if (ObjectUtils.isEmpty(conditionContext.getData()) || ObjectUtils.isEmpty(eventContext.getEventData())) {
            condition.isSatisfied(eventContext, conditionContext);
        }

        var mapCopy = new HashMap<>(conditionContext.getData());
        for (Map.Entry<String, Object> entry : mapCopy.entrySet()) {
            if (entry.getValue() instanceof String valueStr) {
                String processedValue = templateProcessor.process(valueStr, eventContext.getEventData());
                entry.setValue(processedValue);
            }
        }
        condition.isSatisfied(eventContext, new ConditionContext(mapCopy));
        log.debug("ConditionTemplatingInterceptor: Condition data processed successfully.");
    }
}