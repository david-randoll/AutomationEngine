package com.davidrandoll.automation.engine.spring.web.modules.actions.send_http_request;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpRequestEvent;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpResponseEvent;
import com.davidrandoll.automation.engine.spring.web.jackson.flexible_multi_value_map.FlexibleMultiValueMap;
import com.davidrandoll.automation.engine.spring.modules.triggers.always_true.AlwaysTrueTrigger;
import com.davidrandoll.automation.spi.annotation.ContextField;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

@Data
@NoArgsConstructor
@FieldNameConstants
@JsonPropertyOrder({
        SendHttpRequestActionContext.Fields.alias,
        SendHttpRequestActionContext.Fields.description,
        SendHttpRequestActionContext.Fields.url,
        SendHttpRequestActionContext.Fields.method,
        SendHttpRequestActionContext.Fields.headers,
        SendHttpRequestActionContext.Fields.contentType,
        SendHttpRequestActionContext.Fields.body,
        SendHttpRequestActionContext.Fields.storeToVariable,
        SendHttpRequestActionContext.Fields.allowHttpEvent
})
public class SendHttpRequestActionContext implements IActionContext {
    /** Unique identifier for this action */
    private String alias;

    /** Human-readable description of what this action does */
    private String description;

    /** Target URL for the HTTP request. Supports template expressions */
    @ContextField(
        placeholder = "https://api.example.com/endpoint",
        helpText = "Full URL including protocol. Supports template expressions like {{ event.userId }}"
    )
    private String url;

    /** HTTP method to use (GET, POST, PUT, DELETE, etc.) */
    @ContextField(
        helpText = "HTTP method: GET (retrieve), POST (create), PUT (update), DELETE (remove), PATCH (partial update)"
    )
    private HttpMethod method;

    /** HTTP headers to include in the request */
    @JsonAlias({"headers", "header"})
    @FlexibleMultiValueMap
    private HttpHeaders headers;

    /** Content-Type for the request body. Defaults to application/json */
    @ContextField(
        helpText = "MIME type of the request body (e.g., application/json, application/xml, multipart/form-data)"
    )
    private MediaType contentType = MediaType.APPLICATION_JSON;

    /** Request body content. Can be a JSON object, string, or form data depending on contentType */
    @ContextField(
        widget = ContextField.Widget.TEXTAREA,
        placeholder = "{\"key\": \"value\"}",
        helpText = "Request payload. Can be JSON, XML, or form data based on Content-Type. Supports template expressions."
    )
    private Object body;

    /** Variable name to store the HTTP response. If not specified, response is not stored */
    @ContextField(
        placeholder = "responseData",
        helpText = "Variable name to store the response for later use in the automation"
    )
    private String storeToVariable;

    /**
     * If true, the {@link SendHttpRequestAction#canExecute} will process any {@link AEHttpRequestEvent} or {@link AEHttpResponseEvent}.
     * Without this, there could be an infinite loop of events.
     * <br>
     * For example, an automation with an {@link AlwaysTrueTrigger} will always cause the automation to be triggered.
     * This means that when the {@link SendHttpRequestAction#doExecute} is called, it will publish an {@link AEHttpRequestEvent} or {@link AEHttpResponseEvent}.
     * And this will cause the automation to be triggered again which cause the {@link SendHttpRequestAction#doExecute} to be called again.
     */
    private boolean allowHttpEvent = false;

    public Object getBodyByContentType() {
        if (contentType == null) {
            return body;
        }
        if (List.of(APPLICATION_FORM_URLENCODED, MULTIPART_FORM_DATA).contains(contentType)) {
            if (body instanceof Map<?, ?> map) {
                MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (entry.getValue() instanceof List list) {
                        multiValueMap.put(entry.getKey().toString(), list);
                    } else {
                        multiValueMap.add(entry.getKey().toString(), entry.getValue().toString());
                    }
                }
                return multiValueMap;
            }
        }
        return body;
    }
}
