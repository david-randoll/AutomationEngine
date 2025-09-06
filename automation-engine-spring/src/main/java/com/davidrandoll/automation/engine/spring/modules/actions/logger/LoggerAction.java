package com.davidrandoll.automation.engine.spring.modules.actions.logger;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

import java.util.List;

@Slf4j
public class LoggerAction extends PluggableAction<LoggerActionContext> {

    @Override
    public void doExecute(EventContext ec, LoggerActionContext ac) {
        log.atLevel(ac.getLevel()).log("{}", ac.getMessage());
    }

    @Override
    public List<LoggerActionContext> getExamples() {
        return List.of(
                new LoggerActionContext(
                        "simple-info",
                        "Log a simple info message",
                        Level.INFO,
                        "Automation started successfully"
                ),
                new LoggerActionContext(
                        "debug-variables",
                        "Log a debug message with variable placeholders",
                        Level.DEBUG,
                        "Received request with id={{ event.id }} and payload={{ event.body }}"
                )
        );
    }
}