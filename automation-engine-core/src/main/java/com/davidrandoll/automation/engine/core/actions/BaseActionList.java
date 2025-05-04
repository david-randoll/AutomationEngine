package com.davidrandoll.automation.engine.core.actions;

import com.davidrandoll.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.davidrandoll.automation.engine.core.events.EventContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BaseActionList extends ArrayList<IBaseAction> {
    public void executeAll(EventContext eventContext) {
        try {
            for (IBaseAction action : this) {
                action.execute(eventContext);
            }
        } catch (StopActionSequenceException e) {
            // This exception is thrown when the action sequence should be stopped
        }
    }

    public void executeAllAsync(EventContext eventContext) {
        try {
            List<CompletableFuture<Void>> futures = this.stream()
                    .map(action -> CompletableFuture.runAsync(() -> action.execute(eventContext)))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (StopActionSequenceException e) {
            // This exception is thrown when the action sequence should be stopped
        }
    }

    public void executeAllAsync(EventContext eventContext, Executor executor) {
        try {
            List<CompletableFuture<Void>> futures = this.stream()
                    .map(action -> CompletableFuture.runAsync(() -> action.execute(eventContext), executor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (StopActionSequenceException e) {
            // This exception is thrown when the action sequence should be stopped
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