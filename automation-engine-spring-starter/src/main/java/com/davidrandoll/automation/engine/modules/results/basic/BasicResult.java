package com.davidrandoll.automation.engine.modules.results.basic;

import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spi.PluggableResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component("basicResult")
@ConditionalOnMissingBean(name = "basicResult", ignored = BasicResult.class)
public class BasicResult extends PluggableResult<BasicResultContext> {

    @Override
    public Object getExecutionSummary(EventContext ec, BasicResultContext cc) {
        // the templating engine have already resolved the context
        return cc.getResults();
    }
}