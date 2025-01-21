package com.automation.engine.modules.logger;

import com.automation.engine.engine.actions.AbstractAction;
import com.automation.engine.engine.events.EventContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("loggerAction")
public class LoggerAction extends AbstractAction<LoggerActionContext> {

    @Override
    public void execute(EventContext context, LoggerActionContext actionContext) {
        log.info("{}", actionContext.getMessage());
    }
}