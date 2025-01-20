package com.automation.engine.modules.logger;

import com.automation.engine.engine.actions.ActionContext;
import com.automation.engine.engine.actions.ActionExecutor;
import com.automation.engine.engine.events.EventContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("loggerAction")
public class LoggerAction implements ActionExecutor {

    @Override
    public void execute(EventContext context, ActionContext actionContext) {
        var message = actionContext.getData() == null ? "No message" : actionContext.getData().get("message");
        log.info("Executing LoggerAction with message: {}", message);
    }
}