package com.automation.engine.http.modules.actions;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableAction;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Component("sendHttpRequestAction")
@RequiredArgsConstructor
public class SendHttpRequestAction extends PluggableAction<SendHttpRequestActionContext> {
    @Override
    public void execute(EventContext ec, SendHttpRequestActionContext ac) {
        var webClient = WebClient.builder()
                .baseUrl(ac.getUrl())
                .defaultHeaders(headers -> headers.putAll(ac.getHeaders()))
                .build();

        WebClient.ResponseSpec responseSpec = webClient
                .method(ac.getMethod())
                .contentType(ac.getContentType())
                .bodyValue(ac.getBodyByContentType())
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