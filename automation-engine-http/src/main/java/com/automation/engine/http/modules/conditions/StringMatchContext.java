package com.automation.engine.http.modules.conditions;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class StringMatchContext {
    @JsonAlias({"equal", "==", "equals"})
    private String equals;

    @JsonAlias({"notEqual", "!=", "notEquals"})
    private String notEquals;

    @JsonAlias({"in", "contains", "includes", "anyOf", "hasAnyOf", "anyMatch"})
    private List<String> in;

    @JsonAlias({"notIn", "notContains", "notIncludes", "noneOf", "hasNoneOf"})
    private List<String> notIn;

    @JsonAlias({"regex", "matches", "match"})
    private String regex;

    @JsonAlias({"like"})
    private String like;

    @JsonAlias({"exists", "isPresent", "exist"})
    private Boolean exists;

    public boolean matches(String value) {
        if (equals != null && !equals.equalsIgnoreCase(value)) return false;
        if (notEquals != null && notEquals.equalsIgnoreCase(value)) return false;
        if (in != null && in.stream().noneMatch(x -> x.equalsIgnoreCase(value))) return false;
        if (notIn != null && notIn.stream().anyMatch(x -> x.equalsIgnoreCase(value))) return false;
        if (regex != null && !value.matches(regex)) return false;
        // convert any wildcard % or * to regex .*
        if (like != null) {
            String regexLike = like.replaceAll("[*%]", ".*");
            return value.matches(regexLike);
        }
        return true;
    }
}
