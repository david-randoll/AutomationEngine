package com.automation.engine.http.modules.actions.send_http_request;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableAction;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component("sendHttpRequestAction")
@RequiredArgsConstructor
public class SendHttpRequestAction extends PluggableAction<SendHttpRequestActionContext> {
    @Override
    @Async
    public void execute(EventContext ec, SendHttpRequestActionContext ac) {
        log.debug("Executing SendHttpRequestAction: {}", ac.getAlias());
        var webClient = WebClient.builder()
                .baseUrl(ac.getUrl())
                .defaultHeaders(headers -> {
                    if (ObjectUtils.isEmpty(ac.getHeaders())) return;
                    headers.putAll(ac.getHeaders());
                })
                .build();

        var requestBody = ac.getBodyByContentType();
        WebClient.ResponseSpec responseSpec = webClient
                .method(ac.getMethod())
                .contentType(ac.getContentType())
                .bodyValue(requestBody)
                .retrieve();

        var response = responseSpec
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    throw new RuntimeException("Client error: " + clientResponse.statusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                    throw new RuntimeException("Server error: " + clientResponse.statusCode());
                })
                .bodyToMono(JsonNode.class)
                .block();

        if (!ObjectUtils.isEmpty(ac.getStoreToVariable())) {
            ec.addMetadata(ac.getStoreToVariable(), response);
        }
    }
}