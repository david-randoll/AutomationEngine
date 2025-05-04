package com.davidrandoll.automation.engine.templating.extensions;

import io.pebbletemplates.pebble.extension.AbstractExtension;
import io.pebbletemplates.pebble.extension.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("customExtension")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "customExtension", ignored = CustomExtension.class)
public class CustomExtension extends AbstractExtension {
    private final Map<String, Filter> filters;

    @Override
    public Map<String, Filter> getFilters() {
        return filters;
    }
}