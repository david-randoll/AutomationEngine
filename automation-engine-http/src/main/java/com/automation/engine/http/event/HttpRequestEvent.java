package com.automation.engine.http.event;

import com.automation.engine.core.events.IEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.http.HttpHeaders;

import java.util.Map;

@Data
@FieldNameConstants
@AllArgsConstructor
public class HttpRequestEvent implements IEvent {
    private String fullUrl;
    private String path;
    private HttpMethodEnum method;
    private HttpHeaders headers;
    private Map<String, Object> queryParams;
    private Map<String, Object> pathParams;
    private String requestBody;
}