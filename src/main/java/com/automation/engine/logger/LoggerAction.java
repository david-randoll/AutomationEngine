package com.automation.engine.logger;

import com.automation.engine.core.actions.ActionContext;
import com.automation.engine.core.actions.IAction;
import com.automation.engine.core.events.EventContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component("loggerAction")
public class LoggerAction implements IAction {

    @Override
    public void execute(EventContext context) {
        log.info("Executing LoggerAction");
    }

    @Override
    public void execute(EventContext context, ActionContext actionContext) {
        var message = actionContext.getData() == null ? "No message" : actionContext.getData().get("message");
        log.info("Executing LoggerAction with message: {}", message);
    }
}