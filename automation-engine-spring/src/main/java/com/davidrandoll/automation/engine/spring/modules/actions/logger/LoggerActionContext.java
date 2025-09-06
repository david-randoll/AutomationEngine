package com.davidrandoll.automation.engine.spring.modules.actions.logger;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.slf4j.event.Level;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        LoggerActionContext.Fields.alias,
        LoggerActionContext.Fields.description,
        LoggerActionContext.Fields.level,
        LoggerActionContext.Fields.message
})
public class LoggerActionContext implements IActionContext {
    private String alias;
    private String description;
    private Level level = Level.INFO;
    private String message = "No message";
}