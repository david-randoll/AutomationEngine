package com.davidrandoll.automation.engine.core.events;

import java.io.Serializable;

public interface IEvent extends Serializable {
    // Marker interface for events
    // This interface can be extended to define specific event types
    // and can include additional methods as needed
    default String getEventType() {
        return this.getClass().getName();
    }
}
