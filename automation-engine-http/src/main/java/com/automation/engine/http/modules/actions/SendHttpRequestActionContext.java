package com.automation.engine.http.modules.actions;

import com.automation.engine.core.actions.IActionContext;
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
    private HttpHeaders headers;
    private MediaType contentType;
    private Object body;

    private String storeToVariable;

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
