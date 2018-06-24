package com.benbarkay.events;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
}