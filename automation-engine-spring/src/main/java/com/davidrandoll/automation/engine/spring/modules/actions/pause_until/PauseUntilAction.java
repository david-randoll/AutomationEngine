package com.davidrandoll.automation.engine.spring.modules.actions.pause_until;

import com.davidrandoll.automation.engine.core.actions.ActionResult;
import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Action that pauses automation execution until a specific trigger condition is met.
 * Unlike DelayAction (time-based), this is event-driven and waits for a matching event.
 *
 * <p>Usage in YAML:</p>
 * <pre>
 * actions:
 *   - action: pauseUntil
 *     trigger:
 *       trigger: onEvent
 *       eventType: ApprovalEvent
 *       condition: "{{ event.orderId == trigger.orderId }}"
 *     timeoutMillis: 3600000  # Optional: 1 hour timeout
 *     pauseId: "wait-for-approval"  # Optional: named pause point
 * </pre>
 */
@Component("pauseUntil")
@Slf4j
public class PauseUntilAction extends PluggableAction<PauseUntilActionContext> {

    @Override
    public ActionResult executeWithResult(EventContext ec, PauseUntilActionContext ac) {
        if (ac.getTrigger() == null) {
            log.error("PauseUntilAction requires a trigger specification");
            throw new IllegalArgumentException("Trigger cannot be null for pauseUntil action");
        }

        String executionId = ec.getExecutionId().toString();
        
        // Store optional pause ID in metadata for external reference
        if (ac.getPauseId() != null) {
            ec.getMetadata().put("_pauseId", ac.getPauseId());
            log.info("Pausing execution {} (pauseId: {}) until trigger condition is met", 
                     executionId, ac.getPauseId());
        } else {
            log.info("Pausing execution {} until trigger condition is met", executionId);
        }

        // Store pause metadata
        ec.getMetadata().put("_pausedAtMillis", System.currentTimeMillis());
        if (ac.getTimeoutMillis() != null) {
            ec.getMetadata().put("_timeoutMillis", ac.getTimeoutMillis());
            log.debug("Timeout set to {} ms for execution {}", ac.getTimeoutMillis(), executionId);
        }

        // Return pause result with trigger specification
        return ActionResult.pauseUntil(ac.getTrigger(), ac.getTimeoutMillis());
    }

    @Override
    public void doExecute(EventContext ec, PauseUntilActionContext ac) {
        // Not used - we override executeWithResult instead
    }
}
