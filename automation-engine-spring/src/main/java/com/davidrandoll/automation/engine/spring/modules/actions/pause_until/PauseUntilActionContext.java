package com.davidrandoll.automation.engine.spring.modules.actions.pause_until;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.core.triggers.ITrigger;
import lombok.Data;

/**
 * Context for the PauseUntilAction.
 * Allows an automation to pause and wait for a specific trigger condition to be met.
 */
@Data
public class PauseUntilActionContext implements IActionContext {
    /**
     * The alias of the action.
     */
    private String alias;

    /**
     * The description of the action.
     */
    private String description;

    /**
     * The trigger that will resume execution when activated.
     * The automation will pause until an event matching this trigger is received.
     */
    private ITrigger trigger;

    /**
     * Optional timeout in milliseconds.
     * If specified, the paused execution will be removed after this duration.
     * null = no timeout.
     */
    private Long timeoutMillis;

    /**
     * Optional ID to identify this pause point.
     * Can be used for external resume operations or debugging.
     */
    private String pauseId;
}
