package com.automation.engine.templating;

import com.automation.engine.templating.filters.NumberFormatFilter;
import com.automation.engine.templating.filters.TimeFormatFilter;
import io.pebbletemplates.pebble.extension.AbstractExtension;
import io.pebbletemplates.pebble.extension.Filter;

import java.util.Map;

public class CustomExtension extends AbstractExtension {
    @Override
    public Map<String, Filter> getFilters() {
        return Map.of(
                "number_format", new NumberFormatFilter(),
                "time_format", new TimeFormatFilter()
        );
    }
}
