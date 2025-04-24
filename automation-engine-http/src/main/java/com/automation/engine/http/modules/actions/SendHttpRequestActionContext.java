package com.automation.engine.http.modules.actions;

import com.automation.engine.core.actions.IActionContext;
import com.automation.engine.http.event.HttpMethodEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

import java.util.Map;

@Data
@NoArgsConstructor
public class SendHttpRequestActionContext implements IActionContext {
    private String alias;
    private String url;
    private HttpMethod method;
    private Map<String, String> headers;
    private Map<String, String> queryParams;

    private Object body;
}
