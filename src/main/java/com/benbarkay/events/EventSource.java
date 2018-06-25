package com.benbarkay.events;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Facilitates a source of events.
 * @param <T>   The type of events emitted this {@code EventSource}.
 */
public interface EventSource<T> {
    /**
     * The executor of this {@code EventSource}.
     */
    Executor executor();

    /**
     * Subscribes an {@link EventSubscriber} to this event source. Once subscribed,
     * the subscriber will begin receiving events which have been emitted to this source.
     * @param subscriber    The subscriber to subscribe to this source.
     * @param executor      The executor upon which to accept the event.
     * @return  An {@link EventSubscription} instance representing this subscription.
     */
    EventSubscription subscribe(EventSubscriber<T> subscriber, Executor executor);

    /**
     * Subscribes using the same executor as this source. See {@link #subscribe(EventSubscriber, Executor)}
     * for more information.
     */
    default EventSubscription subscribe(EventSubscriber<T> subscriber) {
        return subscribe(subscriber, executor());
    }

    /**
     * Consumes events emitted by this source, using the specified executor.
     * @param consumer  The consumer to use.
     * @param executor  The executor to use.
     * @return  An {@link EventSubscription} instance representing this subscription.
     */
    default EventSubscription consume(Consumer<T> consumer, Executor executor) {
        return subscribe((e, s) -> consumer.accept(e), executor);
    }

    /**
     * Consumes events emitted by this source. See {@link #consume(Consumer, Executor)}
     * for more information.
     */
    default EventSubscription consume(Consumer<T> consumer) {
        return consume(consumer, executor());
    }

    /**
     * Runs the specified {@link Runnable runnable} upon emission of events.
     * @param r         The runnable to run.
     * @param executor  The executor to use.
     * @return  An {@link EventSubscription} instance representing this subscription.
     */
    default EventSubscription run(Runnable r, Executor executor) {
        return consume((e) -> r.run(), executor);
    }

    /**
     * Runs the specified {@link Runnable runnable} upon submission of events. See {@link #run(Runnable, Executor)}
     * for more information.
     */
    default EventSubscription run(Runnable r) {
        return run(r, executor());
    }

    /**
     * Filters this source using the specified executor.
     * @param predicate The predicate used in order to filter events emitted by this source.
     * @param executor  The executor to used for filtering.
     * @return  A source with filtered events.
     */
    default EventSource<T> filter(Predicate<T> predicate, Executor executor) {
        EventBus<T> recipient = EventBus.create(executor());
        subscribe(new FilteringSubscriber<>(predicate, recipient), executor)
            .error(recipient::error);
        return recipient;
    }

    /**
     * Filters this source, using this source's executor. See {@link #filter(Predicate, Executor)}
     * for more information.
     */
    default EventSource<T> filter(Predicate<T> predicate) {
        return filter(predicate, executor());
    }

    /**
     * Filters this source by type. Only events of types assignable from {@code type}
     * will pass this filter.
     * @param type  The type to filter by.
     * @param <F>   The type produced by this filter.
     * @return  A source filtered by the specified type.
     */
    default <F extends T> EventSource<F> filter(Class<F> type) {
        return filter(e -> type.isAssignableFrom(e.getClass()))
                .map(type::cast);
    }

    /**
     * Maps the events of this source using the specified executor.
     * @param fn        The mapping function to use.
     * @param executor  The executor to use for mapping.
     * @param <F>       The type of the mapped objects.
     * @return  A source of mapped objects, mapped by the specified function.
     */
    default <F> EventSource<F> map(Function<T, F> fn, Executor executor) {
        EventBus<F> recipient = EventBus.create(executor());
        subscribe(new MappingSubscriber<>(fn, recipient), executor)
            .error(recipient::error);
        return recipient;
    }

    /**
     * Maps the events of this source. See {@link #map(Function, Executor)} for more information.
     */
    default <F> EventSource<F> map(Function<T, F> fn) {
        return map(fn, executor());
    }

    /**
     * Peeks at the events of this source, using the specified executor.
     * This method differs from {@link #consume(Consumer, Executor)} in that peeking will
     * stop once the returned source is no longer referred to.
     * @param consumer  The consumer to use for peeking.
     * @param executor  The executor to execute peeking on.
     * @return  A source which relays the events of this source.
     */
    default EventSource<T> peek(Consumer<T> consumer, Executor executor) {
        EventBus<T> recipient = EventBus.create(executor());
        subscribe(new PeekingSubscriber<>(consumer, recipient), executor)
            .error(recipient::error);
        return recipient;
    }

    /**
     * Peeks at the events of this source. See {@link #peek(Consumer, Executor)} for more information.
     */
    default EventSource<T> peek(Consumer<T> consumer) {
        return peek(consumer, executor());
    }

    /**
     * Forwards events to the specified recipient.
     * @param recipient The recipient to forward events to.
     * @return  This event source.
     */
    default EventSource<T> forward(EventEmitter<T> recipient) {
        subscribe(new ForwardingSubscriber<>(recipient));
        return this;
    }

    /**
     * Captures the next event of this source, blocking until it is available.
     */
    default CompletableFuture<T> capture() {
        CompletableFuture<T> future = new CompletableFuture<>();
        peek(future::complete).subscribe(EventSubscriber.cancelling());
        return future;
    }

    /**
     * Adds an error handler to this source. An {@code EventSource} emits exceptions when
     * subscribers throw them, and their subscriptions do not have an appropriate handler.
     * Since {@code .map}, {@code .filter}, and {@code .peek} do not provide any means of associating
     * error handlers to their subscriptions, associating with the event source's error handler is the
     * only way to capture their exceptions.
     * @param type      The type of exceptions handled by the specified exception handler.
     * @param consumer  The exception handler to add.
     * @param <E>   The type of exceptions handled by this exception handler.
     * @return  This source.
     */
    <E extends Throwable> EventSource<T> error(Class<E> type, Consumer<E> consumer);

    /**
     * Adds a catch-all exception handler. Calling this method yields the same results as calling
     * {@code error(Throwable.class, (t) -> { ... })}. See {@link #error(Class, Consumer)} for more
     * information.
     */
    default EventSource<T> error(Consumer<Throwable> consumer) {
        return error(Throwable.class, consumer);
    }
}
