package com.davidrandoll.automation.engine.core.actions;

import com.davidrandoll.automation.engine.core.actions.exceptions.StopActionSequenceException;
import com.davidrandoll.automation.engine.core.actions.exceptions.StopAutomationException;
import com.davidrandoll.automation.engine.core.events.EventContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BaseActionList extends ArrayList<IBaseAction> {
    /**
     * Executes all actions using a virtual stack machine.
     * <p>
     * The execution uses a stack of {@link ExecutionFrame}s to track the current position
     * in nested action hierarchies. This allows for:
     * <ul>
     *   <li>Pause/Resume: Execution can be paused and later resumed from the exact position</li>
     *   <li>Nested blocks: Block actions (if-then-else, repeat, sequence) can invoke child actions
     *       without manually managing indices</li>
     * </ul>
     * <p>
     * Actions return {@link ActionResult} to control flow:
     * <ul>
     *   <li>{@code CONTINUE} - Move to the next action in the current frame</li>
     *   <li>{@code PAUSE} - Save state and return (can be resumed later)</li>
     *   <li>{@code STOP} - Clear stack and return immediately</li>
     *   <li>{@code INVOKE} - Push a new frame with child actions onto the stack</li>
     * </ul>
     *
     * @param eventContext The event context for execution
     * @return The final result of execution
     */
    public ActionResult executeAll(EventContext eventContext) {
        Deque<ExecutionFrame> stack = eventContext.getExecutionStack();

        // If stack is empty, this is a fresh execution - push the initial frame
        if (stack.isEmpty()) {
            stack.push(ExecutionFrame.of(this));
        }

        while (!stack.isEmpty()) {
            ExecutionFrame frame = stack.peek();

            // If current frame has no more actions, pop it and continue with parent
            if (!frame.hasMore()) {
                stack.pop();
                continue;
            }

            // Get and execute the current action
            IBaseAction action = frame.currentAction();
            ActionResult result;
            try {
                result = action.execute(eventContext);
            } catch (StopActionSequenceException e) {
                // StopActionSequence only stops the current frame (sequence/block).
                // Pop current frame and continue with parent frame's remaining actions.
                stack.pop();
                continue;
            } catch (StopAutomationException e) {
                // StopAutomation stops the entire automation - clear the stack and rethrow
                // so that Automation.performActions() can handle it properly.
                stack.clear();
                throw e;
            }

            switch (result.status()) {
                case CONTINUE -> {
                    // Advance to the next action in this frame
                    stack.pop();
                    stack.push(frame.next());
                }
                case INVOKE -> {
                    // First advance the current frame (so when we return, we continue from next)
                    stack.pop();
                    stack.push(frame.next());
                    // Then push the child actions as a new frame
                    stack.push(ExecutionFrame.of(result.children()));
                }
                case PAUSE -> {
                    // Advance frame so resume continues from next action
                    stack.pop();
                    stack.push(frame.next());
                    return ActionResult.pause();
                }
                case STOP -> {
                    // Clear the stack and stop execution
                    stack.clear();
                    return ActionResult.stop();
                }
            }
        }
        return ActionResult.continueExecution();
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