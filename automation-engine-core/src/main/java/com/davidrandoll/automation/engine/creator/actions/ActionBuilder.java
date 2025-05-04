package com.davidrandoll.automation.engine.creator.actions;

import com.davidrandoll.automation.engine.core.actions.ActionContext;
import com.davidrandoll.automation.engine.core.actions.BaseActionList;
import com.davidrandoll.automation.engine.core.actions.IAction;
import com.davidrandoll.automation.engine.core.actions.IBaseAction;
import com.davidrandoll.automation.engine.core.actions.interceptors.IActionInterceptor;
import com.davidrandoll.automation.engine.core.actions.interceptors.InterceptingAction;
import com.davidrandoll.automation.engine.core.events.EventContext;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class ActionBuilder {
    private final IActionSupplier supplier;
    private final List<IActionInterceptor> actionInterceptors;

    public BaseActionList resolve(List<Action> actions) {
        var result = new BaseActionList();

        if (isNull(actions)) return result;

        for (var action : actions) {
            IBaseAction newActionInstance = buildAction(action);

            result.add(newActionInstance);
        }

        return result;
    }

    private IBaseAction buildAction(Action action) {
        IAction actionInstance = Optional.ofNullable(supplier.getAction(action.getAction()))
                .orElseThrow(() -> new ActionNotFoundException(action.getAction()));

        var interceptingAction = new InterceptingAction(actionInstance, actionInterceptors);
        var actionContext = new ActionContext(action.getParams());

        return eventContext -> interceptingAction.execute(eventContext, actionContext);
    }

    public void executeActions(EventContext eventContext, List<Action> actions) {
        BaseActionList resolvedActions = resolve(actions);
        resolvedActions.executeAll(eventContext);
    }

    public void executeActionsAsync(EventContext eventContext, List<Action> actions) {
        BaseActionList resolvedActions = resolve(actions);
        resolvedActions.executeAllAsync(eventContext);
    }

    public void executeActionsAsync(EventContext eventContext, List<Action> actions, Executor executor) {
        BaseActionList resolvedActions = resolve(actions);
        resolvedActions.executeAllAsync(eventContext, executor);
    }
}