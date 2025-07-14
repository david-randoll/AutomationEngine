package com.davidrandoll.automation.engine.modules.actions.logger;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoggerActionContext implements IActionContext {
    private String alias;
    private String description;
    private String level = "INFO";
    private String message = "No message";
}