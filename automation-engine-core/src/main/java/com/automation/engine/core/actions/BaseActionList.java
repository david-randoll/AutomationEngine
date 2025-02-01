package com.automation.engine.core.actions;

import com.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.automation.engine.core.events.Event;

import java.util.ArrayList;

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
}