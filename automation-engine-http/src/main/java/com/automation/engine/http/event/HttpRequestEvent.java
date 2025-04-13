package com.automation.engine.http.event;

import com.automation.engine.core.events.IEvent;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;

@Data
@FieldNameConstants
@Builder
public class HttpRequestEvent implements IEvent {
    private String fullUrl;
    private String path;
    private HttpMethodEnum method;
    private HttpHeaders headers;
    private Map<String, Object> queryParams;
    private Map<String, Object> pathParams;
    private String requestBody;

    @JsonAnySetter
    @JsonAnyGetter
    private Map<String, Object> additionalData;

    public void addAdditionalData(@NonNull Map<String, Object> additionalData) {
        if (this.additionalData == null) this.additionalData = new HashMap<>();
        this.additionalData.putAll(additionalData);
    }
}