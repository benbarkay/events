package com.benbarkay.events;

import org.junit.Test;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class EventBusTest {
    @Test
    public void consumeReceivesEmittedEvents() {
        List<String> expected = Arrays.asList("1", "2", "3");
        List<String> actual = new ArrayList<>(expected.size());
        EventBus<String> bus = EventBus.blocking();
        bus.consume(actual::add);

        expected.forEach(bus::emit);
        assertEquals(expected, actual);
    }

    @Test
    public void peekIsGarbageCollectedWhenNoReferenceAndNoSubscribers() {
        List<String> expected = Collections.emptyList();
        List<String> actual = new ArrayList<>(expected.size());
        EventBus<String> bus = EventBus.blocking();
        bus.peek(actual::add);
        System.gc();

        Stream.of("1", "2", "3").forEach(bus::emit);
        assertEquals(expected, actual);
    }

    @Test
    public void peekIsNotGarbageCollectedWhenItHasConsumer() {
        List<String> expected = Arrays.asList("1", "2", "3");
        List<String> actual = new ArrayList<>(expected.size());
        EventBus<String> bus = EventBus.blocking();
        bus.peek(actual::add).consume((s) -> {});
        System.gc();

        expected.forEach(bus::emit);
        assertEquals(expected, actual);
    }

    @Test
    public void demapChainIsNotGarbageCollectedWhenTargetBusExists() {
        List<String> expected = Arrays.asList("1", "2", "3");
        List<String> actual = new ArrayList<>(expected.size());

        EventBus<String> bus = EventBus.blocking();
        bus.consume(actual::add);

        EventEmitter<Object> mappedEmitter = bus.demap(String::valueOf);

        System.gc();
        mappedEmitter.emit(1);
        mappedEmitter.emit(2);
        mappedEmitter.emit(3);

        assertEquals(expected, actual);
    }

    @Test
    public void demapDoesNotHoldStrongReferenceToSource() {
        VariableStrengthReference<EventBus<String>> bus = new VariableStrengthReference<>(EventBus.blocking());
        EventEmitter<Integer> mappedEmitter = bus.value().demap(String::valueOf);
        bus.setStrong(false);

        mappedEmitter.emit(1);
        mappedEmitter.emit(2);
        mappedEmitter.emit(3);

        System.gc();

        assertTrue(bus.isGarbageCollected());
    }

    @Test
    public void captureCapturesNextEvent() throws ExecutionException, InterruptedException {
        String expected = "test";

        EventBus<String> bus = EventBus.blocking();
        CompletableFuture<String> future = bus.capture();
        bus.emit(expected);

        assertEquals(expected, future.get());
    }
}