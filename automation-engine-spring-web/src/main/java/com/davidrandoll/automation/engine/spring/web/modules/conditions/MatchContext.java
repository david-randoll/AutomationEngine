package com.davidrandoll.automation.engine.spring.web.modules.conditions;

import com.davidrandoll.automation.engine.spring.spi.ContextField;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Data
@NoArgsConstructor
public class MatchContext {
    /** Check if the value equals this exact string */
    @ContextField(
        placeholder = "expected value",
        helpText = "Exact match. Value must equal this string exactly"
    )
    @JsonAlias({"equal", "==", "equals"})
    private String equals;

    /** Check if the value does not equal this string */
    @ContextField(
        placeholder = "excluded value",
        helpText = "Negative match. Value must NOT equal this string"
    )
    @JsonAlias({"notEqual", "!=", "notEquals"})
    private String notEquals;

    /** Check if the value matches any string in this list */
    @ContextField(
        helpText = "Value must match one of these options (OR logic)"
    )
    @JsonAlias({"in", "contains", "includes", "anyOf", "hasAnyOf", "anyMatch", "equalsAny"})
    private List<String> in;

    /** Check if the value does not match any string in this list */
    @ContextField(
        helpText = "Value must NOT match any of these options"
    )
    @JsonAlias({"notIn", "notContains", "notIncludes", "noneOf", "hasNoneOf"})
    private List<String> notIn;

    /** Check if the value matches this regular expression pattern */
    @ContextField(
        placeholder = "^[a-zA-Z0-9]+$",
        helpText = "Regular expression pattern to match against the value"
    )
    @JsonAlias({"regex", "matches", "match"})
    private String regex;

    /** Check if the value contains this substring (case-insensitive wildcard match) */
    @ContextField(
        placeholder = "*substring*",
        helpText = "Wildcard match (case-insensitive). Use * for any characters"
    )
    @JsonAlias({"like"})
    private String like;

    /** Check if the value exists (is not null or empty). If true, value must exist; if false, value must not exist */
    @ContextField(
        widget = ContextField.Widget.SWITCH,
        helpText = "Check existence. true = must exist, false = must not exist"
    )
    @JsonAlias({"exists", "isPresent", "exist"})
    private Boolean exists;

    public boolean matches(Object value) {
        if (ObjectUtils.isEmpty(value)) {
            var hasOtherOperation = hasOtherOperations();
            if (!hasOtherOperation)
                return !Boolean.TRUE.equals(this.getExists());
            if (value == null) return false;
        }

        return switch (value) {
            case String str -> matchString(str);
            case List<?> list -> matchArray(list);
            default -> matchString(String.valueOf(value));
        };

    }

    private boolean hasOtherOperations() {
        return this.equals != null
               || this.notEquals != null
               || this.in != null
               || this.notIn != null
               || this.regex != null
               || this.like != null;
    }

    public boolean hasAnyOperations() {
        return hasOtherOperations()
               || this.exists != null;
    }

    private boolean matchArray(List<?> list) {
        // if the value is a list, check if any of the elements match
        if (this.notEquals != null || this.notIn != null) {
            for (var item : list) {
                if (!this.matches(item)) {
                    return false;
                }
            }
            return true;
        }

        for (var item : list) {
            if (this.matches(item)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchString(String str) {
        if (this.exists != null && !this.exists) return false;
        if (this.equals != null && !this.equals.equalsIgnoreCase(str)) return false;
        if (this.notEquals != null && this.notEquals.equalsIgnoreCase(str)) return false;
        if (this.in != null && this.in.stream().noneMatch(x -> x.equalsIgnoreCase(str))) return false;
        if (this.notIn != null && this.notIn.stream().anyMatch(x -> x.equalsIgnoreCase(str))) return false;
        if (this.regex != null && !str.matches(this.regex)) return false;
        // convert any wildcard % or * to regex .*
        if (this.like != null) {
            String regexLike = this.like.replaceAll("[*%]", ".*");
            return str.matches(regexLike);
        }
        return true;
    }
}
