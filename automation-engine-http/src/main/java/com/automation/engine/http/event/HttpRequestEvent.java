package com.automation.engine.http.event;

import com.automation.engine.core.events.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
@AllArgsConstructor
public class HttpRequestEvent extends Event {
    private String fullUrl;
    private String path;
    private HttpMethodEnum method;
    private HttpHeaders headers;
    private Map<String, Object> queryParams;
    private Map<String, Object> pathParams;
    private String requestBody;


    @Override
    @NonNull
    public Map<String, Object> getFieldValue() {
        return Map.of(
                Fields.fullUrl, this.fullUrl,
                Fields.path, this.path,
                Fields.method, this.method,
                Fields.headers, this.headers,
                Fields.queryParams, this.queryParams,
                Fields.pathParams, this.pathParams,
                Fields.requestBody, this.requestBody
        );
    }
}