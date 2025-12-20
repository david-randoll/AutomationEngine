package com.davidrandoll.automation.engine.creator.triggers;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.triggers.BaseTriggerList;
import com.davidrandoll.automation.engine.core.triggers.IBaseTrigger;
import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import com.davidrandoll.automation.engine.core.triggers.TriggerContext;
import com.davidrandoll.automation.engine.core.triggers.interceptors.ITriggerInterceptor;
import com.davidrandoll.automation.engine.core.triggers.interceptors.InterceptingTrigger;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class TriggerBuilder {
    private final ITriggerSupplier supplier;
    private final List<ITriggerInterceptor> triggerInterceptors;

    public BaseTriggerList resolve(List<TriggerDefinition> triggers) {
        var result = new BaseTriggerList();

        if (isNull(triggers))
            return result;

        for (TriggerDefinition trigger : triggers) {
            IBaseTrigger newTriggerInstance = buildTrigger(trigger);
            result.add(newTriggerInstance);
        }

        return result;
    }

    private IBaseTrigger buildTrigger(TriggerDefinition trigger) {
        ITrigger triggerInstance = Optional.ofNullable(supplier.getTrigger(trigger.getTrigger()))
                .orElseThrow(() -> new TriggerNotFoundException(trigger.getTrigger()));

        var interceptingTrigger = new InterceptingTrigger(triggerInstance, triggerInterceptors);

        // Add alias, description, and type to params map for tracing interceptors
        Map<String, Object> params = new HashMap<>(trigger.getParams());
        params.put("__type", trigger.getTrigger()); // Store type for tracing interceptors
        if (trigger.getAlias() != null) {
            params.put("alias", trigger.getAlias());
        }
        if (trigger.getDescription() != null) {
            params.put("description", trigger.getDescription());
        }

        var triggerContext = new TriggerContext(params);
        return event -> interceptingTrigger.isTriggered(event, triggerContext);
    }

    public boolean anyTriggersTriggered(EventContext eventContext, List<TriggerDefinition> triggers) {
        BaseTriggerList resolvedTriggers = resolve(triggers);
        return resolvedTriggers.anyTriggered(eventContext);
    }

    public boolean allTriggersTriggered(EventContext eventContext, List<TriggerDefinition> triggers) {
        BaseTriggerList resolvedTriggers = resolve(triggers);
        return resolvedTriggers.allTriggered(eventContext);
    }

    public boolean noneTriggersTriggered(EventContext eventContext, List<TriggerDefinition> triggers) {
        BaseTriggerList resolvedTriggers = resolve(triggers);
        return resolvedTriggers.noneTriggered(eventContext);
    }
}
