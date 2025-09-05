package com.davidrandoll.automation.engine.spring.modules.actions.logger;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggerAction extends PluggableAction<LoggerActionContext> {

    @Override
    public void doExecute(EventContext ec, LoggerActionContext ac) {
        log.atLevel(ac.getLevel()).log("{}", ac.getMessage());
    }
}