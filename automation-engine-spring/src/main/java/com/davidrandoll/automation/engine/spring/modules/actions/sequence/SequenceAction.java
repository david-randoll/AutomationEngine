package com.davidrandoll.automation.engine.spring.modules.actions.sequence;


import com.davidrandoll.automation.engine.core.events.EventContext;
import com.davidrandoll.automation.engine.spring.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

@Slf4j
@RequiredArgsConstructor
public class SequenceAction extends PluggableAction<SequenceActionContext> {

    @Override
    public void doExecute(EventContext ec, SequenceActionContext ac) {
        if (ObjectUtils.isEmpty(ac.getActions())) return;
        processor.executeActions(ec, ac.getActions());
    }
}
