package com.automation.engine.http.event;

import com.automation.engine.core.events.IEvent;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

@Data
@FieldNameConstants
@Builder
public class HttpResponseEvent implements IEvent {
    private String fullUrl;
    private String path;
    private HttpMethodEnum method;
    private HttpHeaders headers;
    private MultiValueMap<String, String> queryParams;
    private Map<String, String> pathParams;
    private JsonNode requestBody;

    private JsonNode responseBody;
    private HttpStatus responseStatus;
    private Map<String, Object> errorDetail;

    @JsonAnySetter
    @JsonAnyGetter
    private Map<String, Object> additionalData;


    public void addErrorDetail(@NonNull Map<String, Object> errorDetail) {
        this.errorDetail = errorDetail;
        var factory = new ObjectMapper().getNodeFactory();
        var message = errorDetail.getOrDefault("message", "").toString();
        this.responseBody = factory.textNode(message);
    }

    public void addAdditionalData(@NonNull Map<String, Object> additionalData) {
        if (this.additionalData == null) this.additionalData = new HashMap<>();
        this.additionalData.putAll(additionalData);
    }

    public boolean isErrorResponse() {
        return responseStatus != null && (isClientErrorResponse() || isServerErrorResponse());
    }

    public boolean isServerErrorResponse() {
        return responseStatus != null && responseStatus.is5xxServerError();
    }

    public boolean isClientErrorResponse() {
        return responseStatus != null && responseStatus.is4xxClientError();
    }

    public boolean isSuccessResponse() {
        return responseStatus != null && responseStatus.is2xxSuccessful();
    }
}