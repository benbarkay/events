package com.benbarkay.events;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface EventSource<T> {
    Executor executor();
    EventSubscription<T> subscribe(EventSubscriber<T> subscriber, Executor executor);

    default EventSubscription<T> subscribe(EventSubscriber<T> subscriber) {
        return subscribe(subscriber, executor());
    }

    default EventSubscription<T> consume(Consumer<T> consumer, Executor executor) {
        return subscribe((e, s) -> consumer.accept(e), executor);
    }

    default EventSubscription<T> consume(Consumer<T> consumer) {
        return consume(consumer, executor());
    }

    default EventSubscription<T> run(Runnable r, Executor executor) {
        return consume((e) -> r.run(), executor);
    }

    default EventSubscription<T> run(Runnable r) {
        return run(r, executor());
    }

    default EventSource<T> filter(Predicate<T> predicate) {
        EventBus<T> recipient = EventBus.create(executor());
        subscribe(new FilteringSubscriber<>(predicate, recipient))
            .error(recipient::error);
        return recipient;
    }

    default <F extends T> EventSource<F> filter(Class<F> type) {
        return filter(e -> type.isAssignableFrom(e.getClass()))
                .cast(type);
    }

    default <F> EventSource<F> map(Function<T, F> fn) {
        EventBus<F> recipient = EventBus.create(executor());
        subscribe(new MappingSubscriber<>(fn, recipient))
            .error(recipient::error);
        return recipient;
    }

    default <F> EventSource<F> cast(Class<F> type) {
        return map(type::cast);
    }

    default EventSource<T> peek(Consumer<T> consumer) {
        EventBus<T> recipient = EventBus.create(executor());
        subscribe(new PeekingSubscriber<>(consumer, recipient))
            .error(recipient::error);
        return recipient;
    }

    default EventSource<T> forward(EventEmitter<T> recipient) {
        subscribe(new ForwardingSubscriber<>(recipient)).error(recipient::error);
        return this;
    }

    default T first() throws InterruptedException {
        return first(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    default T first(long timeout, TimeUnit timeUnit) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> valueRef = new AtomicReference<>();
        EventSubscription<T> subscription = peek(valueRef::set).run(latch::countDown);
        try {
            latch.await(timeout, timeUnit);
            return valueRef.get();
        } finally {
            subscription.cancel();
        }
    }

    default CompletableFuture<T> firstAsync(long timeout, TimeUnit timeUnit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return first(timeout, timeUnit);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    <E extends Throwable> EventSource<T> error(Class<E> type, Consumer<E> consumer);

    default EventSource<T> error(Consumer<Throwable> consumer) {
        return error(Throwable.class, consumer);
    }
}
