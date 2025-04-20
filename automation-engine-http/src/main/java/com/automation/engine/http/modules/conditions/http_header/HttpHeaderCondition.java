package com.automation.engine.http.modules.conditions.http_header;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.http.modules.conditions.StringMatchContext;
import com.automation.engine.spi.PluggableCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component("httpHeaderCondition")
@RequiredArgsConstructor
public class HttpHeaderCondition extends PluggableCondition<HttpHeaderContext> {
    @Override
    public boolean isSatisfied(EventContext ec, HttpHeaderContext cc) {
        if (!(ec.getEvent() instanceof HttpRequestEvent event)) return false;
        for (Map.Entry<String, StringMatchContext> entry : cc.getHeaders().entrySet()) {
            StringMatchContext operation = entry.getValue();
            List<String> headerValue = Optional.ofNullable(event.getHeaders().get(entry.getKey())).orElse(List.of());
            // check if the config has an exists condition
            if (operation.getExists() && headerValue.isEmpty()) return false;
            var satisfied = headerValue.stream().anyMatch(operation::matches);
            if (!satisfied) return false;
        }
        return true;
    }
}