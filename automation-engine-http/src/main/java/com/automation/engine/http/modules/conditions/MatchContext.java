package com.automation.engine.http.modules.conditions;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Data
@NoArgsConstructor
public class MatchContext {
    @JsonAlias({"equal", "==", "equals"})
    private String equals;

    @JsonAlias({"notEqual", "!=", "notEquals"})
    private String notEquals;

    @JsonAlias({"in", "contains", "includes", "anyOf", "hasAnyOf", "anyMatch", "equalsAny"})
    private List<String> in;

    @JsonAlias({"notIn", "notContains", "notIncludes", "noneOf", "hasNoneOf"})
    private List<String> notIn;

    @JsonAlias({"regex", "matches", "match"})
    private String regex;

    @JsonAlias({"like"})
    private String like;

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
