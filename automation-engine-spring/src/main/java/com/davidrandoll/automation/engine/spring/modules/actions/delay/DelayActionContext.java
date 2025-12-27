package com.davidrandoll.automation.engine.spring.modules.actions.delay;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.spi.annotation.PresentationHint;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.time.Duration;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldNameConstants
@JsonPropertyOrder({
        DelayActionContext.Fields.alias,
        DelayActionContext.Fields.description,
        DelayActionContext.Fields.duration
})
public class DelayActionContext implements IActionContext {
    /** Unique identifier for this action */
    private String alias;

    /** Human-readable description of what this action does */
    private String description;

    /**
     * Duration to wait before executing the next action.
     * The duration is specified using the ISO-8601 format:
     * <pre>
     * P[nY][nM][nD]T[nH][nM][nS]
     * </pre>
     * Where:
     * <ul>
     *     <li><b>P1Y</b> - 1 year</li>
     *     <li><b>P1M</b> - 1 month</li>
     *     <li><b>P1D</b> - 1 day</li>
     *     <li><b>PT1H</b> - 1 hour</li>
     *     <li><b>PT1M</b> - 1 minute</li>
     *     <li><b>PT5S</b> - 5 seconds</li>
     *     <li><b>PT1M30S</b> - 1 minute and 30 seconds</li>
     *     <li><b>PT1H30M</b> - 1 hour and 30 minutes</li>
     *     <li><b>PT1H30M10S</b> - 1 hour, 30 minutes, and 10 seconds</li>
     *     <li><b>PT1H30M10.5S</b> - 1 hour, 30 minutes, and 10.5 seconds</li>
     * </ul>
     * <p>
     * Note: The duration can combine time units (hours, minutes, seconds) and date units (years, months, days).
     */
    @PresentationHint(
        placeholder = "PT5S",
        helpText = "ISO-8601 duration format. Examples: PT5S (5 seconds), PT1M30S (1 minute 30 seconds), PT1H (1 hour)"
    )
    private Duration duration;

}