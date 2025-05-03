package com.automation.engine.http.modules.actions.send_http_request;

import com.automation.engine.core.actions.IActionContext;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.http.event.HttpResponseEvent;
import com.automation.engine.http.jackson.flexible_multi_value_map.FlexibleMultiValueMap;
import com.automation.engine.modules.triggers.always_true.AlwaysTrueTrigger;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class SendHttpRequestActionContext implements IActionContext {
    private String alias;
    private String url;
    private HttpMethod method;

    @JsonAlias({"headers", "header"})
    @FlexibleMultiValueMap
    private HttpHeaders headers;

    private MediaType contentType = MediaType.APPLICATION_JSON;
    private Object body;

    private String storeToVariable;

    /**
     * If true, the {@link SendHttpRequestAction#canExecute} will process any {@link HttpRequestEvent} or {@link HttpResponseEvent}.
     * Without this, there could be an infinite loop of events.
     * <br>
     * For example, an automation with an {@link AlwaysTrueTrigger} will always cause the automation to be triggered.
     * This means that when the {@link SendHttpRequestAction#doExecute} is called, it will publish an {@link HttpRequestEvent} or {@link HttpResponseEvent}.
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
