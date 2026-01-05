package com.davidrandoll.automation.engine.core.actions;

import com.davidrandoll.automation.engine.core.triggers.ITrigger;

import java.util.List;

/**
 * Represents the result of an action execution.
 * Contains a status and optionally a list of child actions to invoke.
 */
public record ActionResult(Status status, List<IBaseAction> children, ITrigger resumeTrigger, Long timeoutMillis) {

    /**
     * The status of an action execution.
     */
    public enum Status {
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
        STOP,

        /**
         * The action wants to invoke child actions.
         * The engine will push a new frame onto the execution stack.
         */
        INVOKE
    }

    // ---- Static factory methods for common results ----

    private static final ActionResult CONTINUE_RESULT = new ActionResult(Status.CONTINUE, null, null, null);
    private static final ActionResult PAUSE_RESULT = new ActionResult(Status.PAUSE, null, null, null);
    private static final ActionResult STOP_RESULT = new ActionResult(Status.STOP, null, null, null);

    // ---- Backward compatibility constants (deprecated, prefer factory methods) ----

    /**
     * @deprecated Use {@link #continueExecution()} instead
     */
    @Deprecated
    public static final ActionResult CONTINUE = CONTINUE_RESULT;

    /**
     * @deprecated Use {@link #pauseExecution()} instead
     */
    @Deprecated
    public static final ActionResult PAUSE = PAUSE_RESULT;

    /**
     * @deprecated Use {@link #stopExecution()} instead
     */
    @Deprecated
    public static final ActionResult STOP = STOP_RESULT;

    /**
     * Continue execution to the next action.
     */
    public static ActionResult continueExecution() {
        return CONTINUE_RESULT;
    }

    /**
     * Pause execution and save state for later resumption.
     */
    public static ActionResult pauseExecution() {
        return PAUSE_RESULT;
    }

    /**
     * Stop execution of the current action sequence.
     */
    public static ActionResult stopExecution() {
        return STOP_RESULT;
    }

    /**
     * Invoke a list of child actions.
     * The engine will execute these before continuing with the next action.
     *
     * @param actions The child actions to invoke
     * @return An ActionResult with INVOKE status
     */
    public static ActionResult invoke(List<IBaseAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return CONTINUE_RESULT;
        }
        return new ActionResult(Status.INVOKE, actions, null, null);
    }

    /**
     * Pause execution until a specific trigger condition is met.
     * The engine will save state and wait for an event matching the trigger.
     *
     * @param trigger The trigger to wait for before resuming
     * @return An ActionResult with PAUSE status and resume trigger
     */
    public static ActionResult pauseUntil(ITrigger trigger) {
        return pauseUntil(trigger, null);
    }

    /**
     * Pause execution until a specific trigger condition is met, with a timeout.
     * The engine will save state and wait for an event matching the trigger.
     *
     * @param trigger The trigger to wait for before resuming
     * @param timeoutMillis Optional timeout in milliseconds (null = no timeout)
     * @return An ActionResult with PAUSE status, resume trigger, and timeout
     */
    public static ActionResult pauseUntil(ITrigger trigger, Long timeoutMillis) {
        if (trigger == null) {
            throw new IllegalArgumentException("Resume trigger cannot be null");
        }
        return new ActionResult(Status.PAUSE, null, trigger, timeoutMillis);
    }

    // ---- Convenience methods for status checking ----

    public boolean isContinue() {
        return status == Status.CONTINUE;
    }

    public boolean isPause() {
        return status == Status.PAUSE;
    }

    public boolean isStop() {
        return status == Status.STOP;
    }

    public boolean isInvoke() {
        return status == Status.INVOKE;
    }
}
