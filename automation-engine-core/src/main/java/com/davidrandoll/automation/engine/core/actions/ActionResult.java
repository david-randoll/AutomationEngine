package com.davidrandoll.automation.engine.core.actions;

/**
 * Represents the result of an action execution.
 */
public enum ActionResult {
    /**
     * Execution should continue to the next action.
     */
    CONTINUE,

    /**
     * Execution should be suspended/paused.
     * The current state will be saved and can be resumed later.
     */
    PAUSE,

    /**
     * Execution should stop immediately.
     * No further actions in the current list will be executed.
     */
    STOP
}
