package com.davidrandoll.automation.engine.modules.results.basic;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.core.result.ResultContext;
import com.davidrandoll.automation.engine.spi.PluggableResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.util.Optional;

@Slf4j
public class BasicResult extends PluggableResult<BasicResultContext> {

    @Override
    public Object getExecutionSummary(EventContext context, ResultContext resultContext) {
        if (ObjectUtils.isEmpty(resultContext.getData())) return JsonNodeFactory.instance.nullNode();
        var node = resultContext.getData();
        if (node.isObject() && !node.isEmpty()) {
            return super.getExecutionSummary(context, resultContext);
        }
        BasicResultContext cc = new BasicResultContext();
        cc.setResults(node);
        return getExecutionSummary(context, cc);
    }

    @Override
    public JsonNode getExecutionSummary(EventContext ec, BasicResultContext cc) {
        // the templating engine have already resolved the context
        return Optional.ofNullable(cc)
                .map(BasicResultContext::getResults)
                .orElse(JsonNodeFactory.instance.nullNode());
    }
}