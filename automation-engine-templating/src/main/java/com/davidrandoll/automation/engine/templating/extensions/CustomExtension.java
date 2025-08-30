package com.davidrandoll.automation.engine.templating.extensions;

import io.pebbletemplates.pebble.extension.AbstractExtension;
import io.pebbletemplates.pebble.extension.Filter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class CustomExtension extends AbstractExtension {
    private final Map<String, Filter> filters;

    @Override
    public Map<String, Filter> getFilters() {
        return filters;
    }
}