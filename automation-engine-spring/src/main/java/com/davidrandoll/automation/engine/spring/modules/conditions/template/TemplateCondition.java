package com.davidrandoll.automation.engine.spring.modules.conditions.template;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

@Slf4j
@RequiredArgsConstructor
public class TemplateCondition extends PluggableCondition<TemplateConditionContext> {
    /**
     * The template is already evaluated via interceptor, so we just need to check if it is true
     */
    @Override
    public boolean isSatisfied(EventContext ec, TemplateConditionContext cc) {
        var result = ObjectUtils.nullSafeEquals(cc.getExpression(), "true");
        log.debug("Template condition is satisfied: {}", result);
        return result;
    }
}