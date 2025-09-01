package com.davidrandoll.automation.engine.spring.config;

import com.davidrandoll.automation.engine.AutomationEngine;
import com.davidrandoll.automation.engine.core.events.publisher.IEventPublisher;
import com.davidrandoll.automation.engine.spring.modules.actions.delay.DelayAction;
import com.davidrandoll.automation.engine.spring.modules.actions.if_then_else.IfThenElseAction;
import com.davidrandoll.automation.engine.spring.modules.actions.logger.LoggerAction;
import com.davidrandoll.automation.engine.spring.modules.actions.parallel.ParallelAction;
import com.davidrandoll.automation.engine.spring.modules.actions.repeat.RepeatAction;
import com.davidrandoll.automation.engine.spring.modules.actions.sequence.SequenceAction;
import com.davidrandoll.automation.engine.spring.modules.actions.stop.StopAction;
import com.davidrandoll.automation.engine.spring.modules.actions.variable.VariableAction;
import com.davidrandoll.automation.engine.spring.modules.actions.wait_for_trigger.WaitForTriggerAction;
import com.davidrandoll.automation.engine.spring.modules.conditions.always_false.AlwaysFalseCondition;
import com.davidrandoll.automation.engine.spring.modules.conditions.always_true.AlwaysTrueCondition;
import com.davidrandoll.automation.engine.spring.modules.conditions.and.AndCondition;
import com.davidrandoll.automation.engine.spring.modules.conditions.not.NotCondition;
import com.davidrandoll.automation.engine.spring.modules.conditions.on_event_type.OnEventTypeCondition;
import com.davidrandoll.automation.engine.spring.modules.conditions.or.OrCondition;
import com.davidrandoll.automation.engine.spring.modules.conditions.template.TemplateCondition;
import com.davidrandoll.automation.engine.spring.modules.conditions.time_based.TimeBasedCondition;
import com.davidrandoll.automation.engine.spring.modules.events.AEEventPublisher;
import com.davidrandoll.automation.engine.spring.modules.events.time_based.TimeBasedEventPublisher;
import com.davidrandoll.automation.engine.spring.modules.results.basic.BasicResult;
import com.davidrandoll.automation.engine.spring.modules.triggers.always_false.AlwaysFalseTrigger;
import com.davidrandoll.automation.engine.spring.modules.triggers.always_true.AlwaysTrueTrigger;
import com.davidrandoll.automation.engine.spring.modules.triggers.on_event_type.OnEventTypeTrigger;
import com.davidrandoll.automation.engine.spring.modules.triggers.template.TemplateTrigger;
import com.davidrandoll.automation.engine.spring.modules.triggers.time_based.TimeBasedTrigger;
import com.davidrandoll.automation.engine.spring.modules.variables.basic.BasicVariable;
import com.davidrandoll.automation.engine.spring.modules.variables.if_them_else.IfThenElseVariable;
import com.davidrandoll.automation.engine.spring.AEConfigProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModulesConfig {
    /*
     * Actions
     */

    @Bean(name = "delayAction")
    @ConditionalOnMissingBean(name = "delayAction", ignored = DelayAction.class)
    public DelayAction delayAction() {
        return new DelayAction();
    }

    @Bean(name = "ifThenElseAction")
    @ConditionalOnMissingBean(name = "ifThenElseAction", ignored = IfThenElseAction.class)
    public IfThenElseAction ifThenElseAction() {
        return new IfThenElseAction();
    }

    @Bean(name = "loggerAction")
    @ConditionalOnMissingBean(name = "loggerAction", ignored = LoggerAction.class)
    public LoggerAction loggerAction() {
        return new LoggerAction();
    }

    @Bean(name = "parallelAction")
    @ConditionalOnMissingBean(name = "parallelAction", ignored = ParallelAction.class)
    public ParallelAction parallelAction(@Autowired(required = false) AEConfigProvider provider) {
        return new ParallelAction(provider);
    }

    @Bean(name = "repeatAction")
    @ConditionalOnMissingBean(name = "repeatAction", ignored = RepeatAction.class)
    public RepeatAction repeatAction() {
        return new RepeatAction();
    }

    @Bean(name = "sequenceAction")
    @ConditionalOnMissingBean(name = "sequenceAction", ignored = SequenceAction.class)
    public SequenceAction sequenceAction() {
        return new SequenceAction();
    }

    @Bean(name = "stopAction")
    @ConditionalOnMissingBean(name = "stopAction", ignored = StopAction.class)
    public StopAction stopAction() {
        return new StopAction();
    }

    @Bean(name = "variableAction")
    @ConditionalOnMissingBean(name = "variableAction", ignored = VariableAction.class)
    public VariableAction variableAction() {
        return new VariableAction();
    }

    @Bean(name = "waitForTriggerAction")
    @ConditionalOnMissingBean(name = "waitForTriggerAction", ignored = WaitForTriggerAction.class)
    public WaitForTriggerAction waitForTriggerAction(@Autowired(required = false) AEConfigProvider provider) {
        return new WaitForTriggerAction(provider);
    }

    /*
     * Conditions
     */
    @Bean(name = "alwaysFalseCondition")
    @ConditionalOnMissingBean(name = "alwaysFalseCondition", ignored = AlwaysFalseCondition.class)
    public AlwaysFalseCondition alwaysFalseCondition() {
        return new AlwaysFalseCondition();
    }

    @Bean(name = "alwaysTrueCondition")
    @ConditionalOnMissingBean(name = "alwaysTrueCondition", ignored = AlwaysTrueCondition.class)
    public AlwaysTrueCondition alwaysTrueCondition() {
        return new AlwaysTrueCondition();
    }

    @Bean(name = "andCondition")
    @ConditionalOnMissingBean(name = "andCondition", ignored = AndCondition.class)
    public AndCondition andCondition() {
        return new AndCondition();
    }

    @Bean(name = "notCondition")
    @ConditionalOnMissingBean(name = "notCondition", ignored = NotCondition.class)
    public NotCondition notCondition() {
        return new NotCondition();
    }

    @Bean(name = "onEventTypeCondition")
    @ConditionalOnMissingBean(name = "onEventTypeCondition", ignored = OnEventTypeCondition.class)
    public OnEventTypeCondition onEventTypeCondition() {
        return new OnEventTypeCondition();
    }

    @Bean(name = "orCondition")
    @ConditionalOnMissingBean(name = "orCondition", ignored = OrCondition.class)
    public OrCondition orCondition() {
        return new OrCondition();
    }

    @Bean(name = "templateCondition")
    @ConditionalOnMissingBean(name = "templateCondition", ignored = TemplateCondition.class)
    public TemplateCondition templateCondition() {
        return new TemplateCondition();
    }

    @Bean(name = "timeCondition")
    @ConditionalOnMissingBean(name = "timeCondition", ignored = TimeBasedCondition.class)
    public TimeBasedCondition timeCondition() {
        return new TimeBasedCondition();
    }

    /*
     * Events
     */
    @Bean(name = "timeBasedEventPublisher")
    @ConditionalOnMissingBean(name = "timeBasedEventPublisher", ignored = TimeBasedEventPublisher.class)
    public TimeBasedEventPublisher timeBasedEventPublisher(AutomationEngine engine, AEConfigProvider configProvider) {
        return new TimeBasedEventPublisher(engine, configProvider);
    }

    @Bean
    @ConditionalOnMissingBean(value = IEventPublisher.class, ignored = AEEventPublisher.class)
    public IEventPublisher aeEventPublisher(ApplicationEventPublisher publisher) {
        return new AEEventPublisher(publisher);
    }

    /*
     * Result
     */
    @Bean(name = "basicResult")
    @ConditionalOnMissingBean(name = "basicResult", ignored = BasicResult.class)
    public BasicResult basicResult() {
        return new BasicResult();
    }

    /*
     * Triggers
     */

    @Bean(name = "alwaysFalseTrigger")
    @ConditionalOnMissingBean(name = "alwaysFalseTrigger", ignored = AlwaysFalseTrigger.class)
    public AlwaysFalseTrigger alwaysFalseTrigger() {
        return new AlwaysFalseTrigger();
    }

    @Bean(name = "alwaysTrueTrigger")
    @ConditionalOnMissingBean(name = "alwaysTrueTrigger", ignored = AlwaysTrueTrigger.class)
    public AlwaysTrueTrigger alwaysTrueTrigger() {
        return new AlwaysTrueTrigger();
    }

    @Bean(name = "onEventTypeTrigger")
    @ConditionalOnMissingBean(name = "onEventTypeTrigger", ignored = OnEventTypeTrigger.class)
    public OnEventTypeTrigger onEventTypeTrigger() {
        return new OnEventTypeTrigger();
    }

    @Bean(name = "templateTrigger")
    @ConditionalOnMissingBean(name = "templateTrigger", ignored = TemplateTrigger.class)
    public TemplateTrigger templateTrigger() {
        return new TemplateTrigger();
    }

    @Bean(name = "timeTrigger")
    @ConditionalOnMissingBean(name = "timeTrigger", ignored = TimeBasedTrigger.class)
    public TimeBasedTrigger timeTrigger() {
        return new TimeBasedTrigger();
    }

    /*
     * Variables
     */
    @Bean(name = "basicVariable")
    @ConditionalOnMissingBean(name = "basicVariable", ignored = BasicVariable.class)
    public BasicVariable basicVariable() {
        return new BasicVariable();
    }

    @Bean(name = "ifThenElseVariable")
    @ConditionalOnMissingBean(name = "ifThenElseVariable", ignored = IfThenElseVariable.class)
    public IfThenElseVariable ifThenElseVariable() {
        return new IfThenElseVariable();
    }
}
