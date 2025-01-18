package com.automation.engine.actions;

import com.automation.engine.events.EventContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class LoggerAction implements IAction {
    private final String message;

    @Override
    public void execute(EventContext context) {
        log.info("Executing LoggerAction with message: {}", message);
    }
}