package com.automation.engine.modules.actions.logger;


import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component("loggerAction")
@ConditionalOnMissingBean(name = "loggerAction", ignored = LoggerAction.class)
public class LoggerAction extends PluggableAction<LoggerActionContext> {

    @Override
    public void doExecute(EventContext ec, LoggerActionContext ac) {
        switch (ac.getLevel().toUpperCase()) {
            case "TRACE" -> log.trace("{}", ac.getMessage());
            case "DEBUG" -> log.debug("{}", ac.getMessage());
            case "INFO" -> log.info("{}", ac.getMessage());
            case "WARN" -> log.warn("{}", ac.getMessage());
            case "ERROR" -> log.error("{}", ac.getMessage());
            default -> log.info("{}", ac.getMessage());
        }
    }
}