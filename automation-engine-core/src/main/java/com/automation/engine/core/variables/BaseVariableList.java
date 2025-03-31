package com.automation.engine.core.variables;

import com.automation.engine.core.events.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BaseVariableList extends ArrayList<IBaseVariable> {
    public void setVariables(Event event) {
        for (IBaseVariable variable : this) {
            variable.setVariable(event);
        }
    }

    public void setVariablesAsync(Event event) {
        List<CompletableFuture<Void>> futures = this.stream()
                .map(variable -> CompletableFuture.runAsync(() -> variable.setVariable(event)))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    public void setVariablesAsync(Event event, Executor executor) {
        List<CompletableFuture<Void>> futures = this.stream()
                .map(variable -> CompletableFuture.runAsync(() -> variable.setVariable(event), executor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    public static BaseVariableList of(IBaseVariable... variables) {
        BaseVariableList variableList = new BaseVariableList();
        Collections.addAll(variableList, variables);
        return variableList;
    }

    public static BaseVariableList of(List<IBaseVariable> variables) {
        BaseVariableList variableList = new BaseVariableList();
        variableList.addAll(variables);
        return variableList;
    }
}