package com.davidrandoll.automation.engine.modules.actions.logger;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableAction;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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