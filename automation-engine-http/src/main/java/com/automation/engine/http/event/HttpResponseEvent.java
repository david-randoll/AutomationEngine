package com.automation.engine.http.event;

import com.automation.engine.core.events.IEvent;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

import java.util.Map;

@Data
@FieldNameConstants
@Builder
public class HttpResponseEvent implements IEvent {
    private String fullUrl;
    private String path;
    private HttpMethodEnum method;
    private HttpHeaders headers;
    private Map<String, Object> queryParams;
    private Map<String, Object> pathParams;
    private String requestBody;
    private String responseBody;
    private HttpStatus responseStatus;
    private Map<String, Object> errorDetail;

    public void addErrorDetail(@NonNull Map<String, Object> errorDetail) {
        this.errorDetail = errorDetail;
        this.responseBody = errorDetail.getOrDefault("message", "").toString();
    }
}