package com.automation.engine.modules.actions.sequence;


import com.automation.engine.core.events.EventContext;
import com.automation.engine.spi.PluggableAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Slf4j
@Component("sequenceAction")
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "sequenceAction", ignored = SequenceAction.class)
public class SequenceAction extends PluggableAction<SequenceActionContext> {

    @Override
    public void execute(EventContext ec, SequenceActionContext ac) {
        if (ObjectUtils.isEmpty(ac.getActions())) return;
        processor.executeActions(ec, ac.getActions());
    }
}
