package com.automation.engine.modules.action_building_block.repeat;

import com.automation.engine.core.actions.AbstractAction;
import com.automation.engine.core.events.Event;
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
    public void execute(Event event, RepeatActionContext context) {
        if (ObjectUtils.isEmpty(context.getActions())) return;
        for (int i = 0; i < context.getCount(); i++) {
            resolver.executeActions(event, context.getActions());
        }

        if (context.hasWhileConditions()) {
            while (resolver.allConditionsSatisfied(event, context.getWhileConditions())) {
                resolver.executeActions(event, context.getActions());
            }
        }

        if (context.hasUntilConditions()) {
            while (!resolver.allConditionsSatisfied(event, context.getUntilConditions())) {
                resolver.executeActions(event, context.getActions());
            }
        }

        if (context.hasForEach()) {
            for (Object item : context.getForEach()) {
                event.addVariable("item", item);
                resolver.executeActions(event, context.getActions());
                event.removeVariable("item");
            }
        }
    }
}