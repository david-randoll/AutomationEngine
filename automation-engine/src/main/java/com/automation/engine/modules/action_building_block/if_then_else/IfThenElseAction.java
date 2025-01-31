package com.automation.engine.modules.action_building_block.if_then_else;

import com.automation.engine.core.actions.AbstractAction;
import com.automation.engine.core.actions.IBaseAction;
import com.automation.engine.core.conditions.IBaseCondition;
import com.automation.engine.core.events.Event;
import com.automation.engine.factory.resolver.DefaultAutomationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component("ifThenElseAction")
@RequiredArgsConstructor
public class IfThenElseAction extends AbstractAction<IfThenElseActionContext> {
    private final DefaultAutomationResolver resolver;

    @Override
    public void execute(Event eventContext, IfThenElseActionContext actionContext) {
        if (ObjectUtils.isEmpty(actionContext.getIfBlock().getConditions())) return;
        List<IBaseCondition> conditions = resolver.buildConditionsList(actionContext.getIfBlock().getConditions());
        boolean isSatisfied = conditions.stream().allMatch(condition -> condition.isSatisfied(eventContext));
        if (isSatisfied) {
            if (ObjectUtils.isEmpty(actionContext.getThenBlock().getActions())) return;
            List<IBaseAction> actions = resolver.buildActionsList(actionContext.getThenBlock().getActions());
            actions.forEach(action -> action.execute(eventContext));
        } else {
            if (ObjectUtils.isEmpty(actionContext.getElseBlock().getActions())) return;
            List<IBaseAction> actions = resolver.buildActionsList(actionContext.getElseBlock().getActions());
            actions.forEach(action -> action.execute(eventContext));
        }
    }
}