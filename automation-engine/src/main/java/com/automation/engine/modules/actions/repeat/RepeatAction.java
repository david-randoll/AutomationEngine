package com.automation.engine.modules.actions.repeat;

import com.automation.engine.core.actions.AbstractAction;
import com.automation.engine.core.events.EventContext;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Slf4j
@Component("repeatAction")
@RequiredArgsConstructor
public class RepeatAction extends AbstractAction<RepeatActionContext> {
    private final DefaultAutomationResolver resolver;

    @Override
    public void execute(EventContext eventContext, RepeatActionContext actionContext) {
        if (ObjectUtils.isEmpty(actionContext.getActions())) return;
        for (int i = 0; i < actionContext.getCount(); i++) {
            resolver.executeActions(eventContext, actionContext.getActions());
        }

        if (actionContext.hasWhileConditions()) {
            while (resolver.allConditionsSatisfied(eventContext, actionContext.getWhileConditions())) {
                resolver.executeActions(eventContext, actionContext.getActions());
            }
        }

        if (actionContext.hasUntilConditions()) {
            while (!resolver.allConditionsSatisfied(eventContext, actionContext.getUntilConditions())) {
                resolver.executeActions(eventContext, actionContext.getActions());
            }
        }

        if (actionContext.hasForEach()) {
            for (Object item : actionContext.getForEach()) {
                eventContext.addMetadata("item", item);
                resolver.executeActions(eventContext, actionContext.getActions());
                eventContext.removeMetadata("item");
            }
        }
    }
}