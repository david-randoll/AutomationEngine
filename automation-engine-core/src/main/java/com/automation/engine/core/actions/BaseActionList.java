package com.automation.engine.core.actions;

import com.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.automation.engine.core.events.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BaseActionList extends ArrayList<IBaseAction> {
    public void executeAll(Event event) {
        try {
            for (IBaseAction action : this) {
                action.execute(event);
            }
        } catch (StopActionSequenceException e) {
            // This exception is thrown when the action sequence should be stopped
        }
    }

    public void executeAllAsync(Event event) {
        try {
            List<CompletableFuture<Void>> futures = this.stream()
                    .map(action -> CompletableFuture.runAsync(() -> action.execute(event)))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (StopActionSequenceException e) {
            // This exception is thrown when the action sequence should be stopped
        }
    }

    public void executeAllAsync(Event event, Executor executor) {
        try {
            List<CompletableFuture<Void>> futures = this.stream()
                    .map(action -> CompletableFuture.runAsync(() -> action.execute(event), executor))
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