package com.davidrandoll.automation.engine.core.actions;

import com.davidrandoll.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.davidrandoll.automation.engine.core.events.EventContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BaseActionList extends ArrayList<IBaseAction> {
    public ActionResult executeAll(EventContext eventContext) {
        int startIndex = 0;
        if (!eventContext.getExecutionStack().isEmpty()) {
            startIndex = eventContext.getExecutionStack().pop();
        }

        try {
            for (int i = startIndex; i < this.size(); i++) {
                IBaseAction action = this.get(i);
                int stackSizeBefore = eventContext.getExecutionStack().size();
                ActionResult result = action.execute(eventContext);

                if (result == ActionResult.PAUSE) {
                    int stackSizeAfter = eventContext.getExecutionStack().size();
                    if (stackSizeAfter == stackSizeBefore) {
                        // Leaf action returned PAUSE, resume from next
                        eventContext.getExecutionStack().push(i + 1);
                    } else {
                        // Block action returned PAUSE, it already pushed its state, so we push our current index
                        eventContext.getExecutionStack().push(i);
                    }
                    return ActionResult.PAUSE;
                }

                if (result == ActionResult.STOP) {
                    return ActionResult.STOP;
                }
            }
        } catch (StopActionSequenceException e) {
            // This exception is thrown when the action sequence should be stopped
            return ActionResult.STOP;
        }
        return ActionResult.CONTINUE;
    }

    public void executeAllAsync(EventContext eventContext) {
        try {
            List<CompletableFuture<ActionResult>> futures = this.stream()
                    .map(action -> CompletableFuture.supplyAsync(() -> action.execute(eventContext)))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (java.util.concurrent.CompletionException e) {
            if (!(e.getCause() instanceof StopActionSequenceException)) {
                throw e;
            }
            // StopActionSequenceException is caught and suppressed
        }
    }

    public void executeAllAsync(EventContext eventContext, Executor executor) {
        try {
            List<CompletableFuture<ActionResult>> futures = this.stream()
                    .map(action -> CompletableFuture.supplyAsync(() -> action.execute(eventContext), executor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (java.util.concurrent.CompletionException e) {
            if (!(e.getCause() instanceof StopActionSequenceException)) {
                throw e;
            }
            // StopActionSequenceException is caught and suppressed
        }
    }

    public static BaseActionList of(IBaseAction... actions) {
        BaseActionList actionList = new BaseActionList();
        Collections.addAll(actionList, actions);
        return actionList;
    }

    public static BaseActionList of(List<IBaseAction> actions) {
        BaseActionList actionList = new BaseActionList();
        actionList.addAll(actions);
        return actionList;
    }
}