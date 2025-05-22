package com.davidrandoll.automation.engine.todo_example.automation_flow.actions.send_email;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("sendEmailAction")
@RequiredArgsConstructor
public class SendEmailAction extends PluggableAction<SendEmailActionContext> {

    @Override
    public void doExecute(EventContext ec, SendEmailActionContext ac) {
        log.info("[FAKE EMAIL] To: {}, Subject: {}, Body: {}", ac.getTo(), ac.getSubject(), ac.getBody());
    }
}

