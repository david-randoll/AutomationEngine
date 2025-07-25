package com.davidrandoll.automation.engine.modules.conditions.template;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Slf4j
@Component("templateCondition")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "templateCondition", ignored = TemplateCondition.class)
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