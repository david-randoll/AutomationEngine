package com.automation.engine.http.modules.actions;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component("sendHttpRequestAction")
@RequiredArgsConstructor
public class SendHttpRequestAction extends PluggableAction<SendHttpRequestActionContext> {
    private final RestTemplate restTemplate;

    @Override
    public void execute(EventContext ec, SendHttpRequestActionContext ac) {

    }
}