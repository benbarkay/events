package com.benbarkay.events;

import java.util.function.Function;

/**
 * Facilitates event emission.
 *
 * @param <T> The type of events emitted by this emitter.
 */
public interface EventEmitter<T> {
    /**
     * Emits the specified event to subscribers.
     *
     * @param event The event to emit.
     */
    void emit(T event);

    /**
     * Emits the specified error to subscribers.
     *
     * @param t The throwable to emit.
     */
    void error(Throwable t);

    /**
     * Demaps this emitter to an emitter of another type. The returned emitter
     * will then use the mapping function in order to emit to this emitter.
     *
     * @param fn    The mapping function to use.
     * @param <F>   The new type of this emitter.
     * @return  A new emitter that uses the specified mapping function in order to
     *          emit to this emitter.
     */
    default <F> EventEmitter<F> demap(Function<F, T> fn) {
        EventBus<F> bus = EventBus.blocking();
        bus.map(fn)
                .forward(this);
        return bus;
    }

    /**
     * Whether or not this emitter has any subscribers.
     */
    boolean hasSubscribers();
}
