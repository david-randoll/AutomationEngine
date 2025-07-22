package com.davidrandoll.automation.engine.spring.web.modules.triggers.on_http_path_exists;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.web.events.AEHttpRequestEvent;
import com.davidrandoll.automation.engine.spi.PluggableTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("onHttpPathExistsTrigger")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "onHttpPathExistsTrigger", ignored = OnHttpPathExistsTrigger.class)
public class OnHttpPathExistsTrigger extends PluggableTrigger<OnHttpPathExistsTriggerContext> {
    @Override
    public boolean isTriggered(EventContext ec, OnHttpPathExistsTriggerContext tc) {
        if (!(ec.getEvent() instanceof AEHttpRequestEvent event)) return false;
        if (event.getPath() == null) return false;

        // was the endpoint found?
        if (!event.isEndpointExists()) return false;
        if (!ObjectUtils.isEmpty(tc.getMethods())) {
            // check the list by regex
            if (tc.getMethods().stream().noneMatch(method -> event.getMethod().equals(method))) {
                return false;
            }
        }
        if (!ObjectUtils.isEmpty(tc.getPaths())) {
            // check the list by regex
            if (tc.getPaths().stream().noneMatch(path -> event.getPath().matches(path))) {
                return false;
            }
        }

        return true;
    }
}