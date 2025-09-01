package com.davidrandoll.automation.engine.modules.triggers.template;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableTrigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

@Slf4j
@RequiredArgsConstructor
public class TemplateTrigger extends PluggableTrigger<TemplateTriggerContext> {
    /**
     * The template is already evaluated via interceptor, so we just need to check if it is true
     */
    @Override
    public boolean isTriggered(EventContext ec, TemplateTriggerContext tc) {
        var result = ObjectUtils.nullSafeEquals(tc.getExpression(), "true");
        log.debug("Template condition is satisfied: {}", result);
        return result;
    }
}