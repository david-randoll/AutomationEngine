package com.davidrandoll.automation.engine.templating.extensions.filters;

import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

@Component("number_format")
@ConditionalOnMissingBean(name = "number_format", ignored = NumberFormatFilter.class)
public class NumberFormatFilter implements Filter {

    @Override
    public List<String> getArgumentNames() {
        return List.of("decimalPlaces");
    }

    @Override
    public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) throws PebbleException {
        if (input instanceof Number) {
            // Get the decimalPlaces argument, default to 2 if not provided
            int decimalPlaces = 2; // default value
            if (args.containsKey("decimalPlaces")) {
                Object decimalArg = args.get("decimalPlaces");
                if (decimalArg instanceof Number num) {
                    decimalPlaces = num.intValue();
                }
            }

            // Build the pattern dynamically based on decimal places
            StringBuilder pattern = new StringBuilder("#");
            if (decimalPlaces > 0) {
                pattern.append(".");
                pattern.append("0".repeat(decimalPlaces));
            }

            // Format the number
            DecimalFormat decimalFormat = new DecimalFormat(pattern.toString());
            return decimalFormat.format(input);
        }
        return input; // return the input if it's not a number
    }
}
