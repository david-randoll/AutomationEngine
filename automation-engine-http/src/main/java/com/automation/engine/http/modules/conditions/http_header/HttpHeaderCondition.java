package com.automation.engine.http.modules.conditions.http_header;

import com.automation.engine.core.events.EventContext;
import com.automation.engine.http.event.HttpRequestEvent;
import com.automation.engine.http.utils.StringMatcher;
import com.automation.engine.spi.PluggableCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("httpHeaderCondition")
@RequiredArgsConstructor
public class HttpHeaderCondition extends PluggableCondition<HttpHeaderContext> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean isSatisfied(EventContext ec, HttpHeaderContext cc) {
        if (!(ec.getEvent() instanceof HttpRequestEvent event)) return false;
        return StringMatcher.matchesCondition(cc.getHeaders(), event.getHeaders(), objectMapper);

//        for (Map.Entry<String, StringMatchContext> entry : cc.getHeaders().entrySet()) {
//            StringMatchContext operation = entry.getValue();
//            List<String> headerValue = Optional.ofNullable(event.getHeaders().get(entry.getKey())).orElse(List.of());
//            // check if the config has an exists condition
//            if (ObjectUtils.isEmpty(headerValue)) {
//                return !Boolean.TRUE.equals(operation.getExists());
//            }
//            var satisfied = headerValue.stream().anyMatch(operation::matches);
//            if (!satisfied) return false;
//        }
//        return true;
    }
}