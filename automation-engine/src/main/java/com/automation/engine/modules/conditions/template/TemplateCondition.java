package com.automation.engine.modules.conditions.template;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Slf4j
@Component("templateCondition")
@RequiredArgsConstructor
public class TemplateCondition extends PluggableCondition<TemplateConditionContext> {
    /**
     * The template is already evaluated via interceptor, so we just need to check if it is true
     */
    @Override
    public boolean isSatisfied(EventContext eventContext, TemplateConditionContext conditionContext) {
        var result = ObjectUtils.nullSafeEquals(conditionContext.getExpression(), "true");
        log.debug("Template condition is satisfied: {}", result);
        return result;
    }
}