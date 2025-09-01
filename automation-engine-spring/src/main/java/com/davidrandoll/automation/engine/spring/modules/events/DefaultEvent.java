package com.davidrandoll.automation.engine.spring.modules.events;

import com.davidrandoll.automation.engine.core.events.IEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class DefaultEvent implements IEvent {

}