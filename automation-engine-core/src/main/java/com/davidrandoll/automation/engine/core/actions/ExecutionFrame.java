package com.davidrandoll.automation.engine.core.actions;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a single frame in the execution stack.
 * Each frame tracks a list of actions and the current execution index.
 *
 * @param actions The list of actions to execute in this frame
 * @param index   The current index in the action list (0-based)
 */
public record ExecutionFrame(List<IBaseAction> actions, int index) implements Serializable {

    /**
     * Creates a new frame with the same actions but a different index.
     *
     * @param newIndex The new index to use
     * @return A new ExecutionFrame with the updated index
     */
    public ExecutionFrame withIndex(int newIndex) {
        return new ExecutionFrame(actions, newIndex);
    }

    /**
     * Creates a new frame starting at index 0.
     *
     * @param actions The list of actions for the frame
     * @return A new ExecutionFrame starting at index 0
     */
    public static ExecutionFrame of(List<IBaseAction> actions) {
        return new ExecutionFrame(actions, 0);
    }

    /**
     * Checks if the frame has more actions to execute.
     *
     * @return true if there are more actions, false otherwise
     */
    public boolean hasMore() {
        return index < actions.size();
    }

    /**
     * Gets the current action to execute.
     *
     * @return The current action, or null if no more actions
     */
    public IBaseAction currentAction() {
        return hasMore() ? actions.get(index) : null;
    }

    /**
     * Creates a new frame advanced to the next action.
     *
     * @return A new ExecutionFrame with index + 1
     */
    public ExecutionFrame next() {
        return withIndex(index + 1);
    }
}
