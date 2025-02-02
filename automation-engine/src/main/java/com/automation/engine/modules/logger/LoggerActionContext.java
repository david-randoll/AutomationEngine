package com.automation.engine.modules.logger;

import com.automation.engine.core.actions.IActionContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoggerActionContext implements IActionContext {
    private String alias;
    private String message = "No message";
}