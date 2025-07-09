package com.davidrandoll.automation.engine.http.modules.actions.send_http_request;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.http.events.AEHttpRequestEvent;
import com.davidrandoll.automation.engine.http.events.AEHttpResponseEvent;
import com.davidrandoll.automation.engine.spi.PluggableAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.atomic.AtomicReference;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

@Slf4j
@Component("sendHttpRequestAction")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "sendHttpRequestAction", ignored = SendHttpRequestAction.class)
@ConditionalOnClass(WebClient.class)
public class SendHttpRequestAction extends PluggableAction<SendHttpRequestActionContext> {
    private final ObjectMapper mapper;

    @Override
    public boolean canExecute(EventContext ec, SendHttpRequestActionContext ac) {
        if (isFalse(ac.isAllowHttpEvent())) {
            log.debug("SendHttpRequestAction is not allowed to process HTTP events: {}", ac.getAlias());
            if (ec.getEvent() instanceof AEHttpRequestEvent) return false;
            if (ec.getEvent() instanceof AEHttpResponseEvent) return false;
        }

        if (ObjectUtils.isEmpty(ac.getUrl())) {
            log.warn("URL is empty for SendHttpRequestAction: {}", ac.getAlias());
            return false;
        }

        if (ObjectUtils.isEmpty(ac.getMethod())) {
            log.warn("HTTP method is empty for SendHttpRequestAction: {}", ac.getAlias());
            return false;
        }

        return true;
    }

    @Override
    public void doExecute(EventContext ec, SendHttpRequestActionContext ac) {
        log.debug("Executing SendHttpRequestAction: {}", ac.getAlias());
        var webClient = WebClient.builder()
                .baseUrl(ac.getUrl())
                .defaultHeaders(headers -> {
                    if (ObjectUtils.isEmpty(ac.getHeaders())) return;
                    headers.putAll(ac.getHeaders());
                })
                .build();

        var requestBody = ac.getBodyByContentType();
        var requestBodySpec = webClient
                .method(ac.getMethod())
                .contentType(ac.getContentType());

        WebClient.RequestHeadersSpec<?> headersSpec = requestBodySpec;
        if (requestBody != null) {
            headersSpec = requestBodySpec.bodyValue(requestBody);
        }

        var responseHolder = new AtomicReference<ResponseEntity<String>>();
        var contentTypeHolder = new AtomicReference<String>();

        headersSpec.exchangeToMono(response -> {
            var ct = response.headers().contentType()
                    .map(MediaType::toString)
                    .orElse("");
            contentTypeHolder.set(ct);

            return response.toEntity(String.class)
                    .doOnNext(responseHolder::set);
        }).block();

        String responseContentType = contentTypeHolder.get();
        ResponseEntity<String> strResponse = responseHolder.get();

        JsonNode response;
        if (responseContentType != null && responseContentType.contains("json")) {
            response = mapper.valueToTree(strResponse.getBody());
        } else {
            // string response
            response = mapper.getNodeFactory().textNode(strResponse.getBody());
        }

        if (!ObjectUtils.isEmpty(ac.getStoreToVariable())) {
            ec.addMetadata(ac.getStoreToVariable(), response);
        }
    }
}