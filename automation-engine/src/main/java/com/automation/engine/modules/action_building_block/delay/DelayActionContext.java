package com.automation.engine.modules.action_building_block.delay;

import com.automation.engine.core.actions.IActionContext;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DelayActionContext implements IActionContext {
    private String alias;
    private Duration duration;
}