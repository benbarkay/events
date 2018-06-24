package com.benbarkay.events;

/**
 * A subscriber to an event.
 * @param <T>   The type of events this subscriber handles.
 */
public interface EventSubscriber<T> {
    /**
     * Accepts an event.
     * @param event         The event to accept.
     * @param subscription  The subscription which triggered the event.
     */
    void accept(T event, EventSubscription subscription);
}
