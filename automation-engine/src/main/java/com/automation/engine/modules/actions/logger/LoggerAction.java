package com.automation.engine.modules.actions.logger;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.AbstractAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("loggerAction")
public class LoggerAction extends AbstractAction<LoggerActionContext> {

    @Override
    public void execute(EventContext eventContext, LoggerActionContext actionContext) {
        log.info("{}", actionContext.getMessage());
    }
}