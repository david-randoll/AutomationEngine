package com.automation.engine.templating.extensions;

import io.pebbletemplates.pebble.extension.AbstractExtension;
import io.pebbletemplates.pebble.extension.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomExtension extends AbstractExtension {
    private final Map<String, Filter> filters;

    @Override
    public Map<String, Filter> getFilters() {
        return filters;
    }
}