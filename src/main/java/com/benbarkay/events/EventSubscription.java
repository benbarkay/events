package com.benbarkay.events;

import java.util.function.Consumer;

/**
 * Represents a subscription to an event.
 */
public interface EventSubscription {

    /**
     * Listens to exceptions thrown by the subscriber of this subscription.
     * @param type      The type of exception to catch.
     * @param consumer  The consumer of the exception.
     * @param <E>   The type of the exception being caught.
     * @return  This {@code EventSubscription} instance.
     */
    <E extends Throwable> EventSubscription error(Class<E> type, Consumer<E> consumer);

    /**
     * Adds a catch-all exception consumer. The result is identical to calling
     * {@code error(Throwable.class, (throwable) -> ...)}.
     * @param consumer  The consumer of the exception.
     * @return  This {@code EventSubscription} instance.
     */
    default EventSubscription error(Consumer<Throwable> consumer) {
        return error(Throwable.class, consumer);
    }

    /**
     * Cancels this event subscription.
     * @return  {@code true} if the subscription was successfully cancelled,
     *          or {@code false} if the subscription was already cancelled.
     */
    boolean cancel();
}
