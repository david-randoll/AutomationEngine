package com.automation.engine.modules.triggers.template;

import com.automation.engine.core.events.Event;
import com.automation.engine.core.triggers.AbstractTrigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Slf4j
@Component("templateTrigger")
@RequiredArgsConstructor
public class TemplateTrigger extends AbstractTrigger<TemplateTriggerContext> {
    /**
     * The template is already evaluated via interceptor, so we just need to check if it is true
     */
    @Override
    public boolean isTriggered(Event event, TemplateTriggerContext context) {
        var result = ObjectUtils.nullSafeEquals(context.getExpression(), "true");
        log.debug("Template condition is satisfied: {}", result);
        return result;
    }
}