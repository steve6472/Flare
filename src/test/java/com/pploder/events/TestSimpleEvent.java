package com.pploder.events;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class TestSimpleEvent
{
    @Test
    public void testTriggerWithNull()
    {
        Event<Void> event = new SimpleEvent<>();

        AtomicBoolean executed = new AtomicBoolean();
        event.addListener(_ -> executed.set(true));

        event.trigger(null);

        Assertions.assertTrue(executed.get());
    }

    @Test
    public void testTriggerNoArg()
    {
        Event<Void> event = new SimpleEvent<>();

        AtomicBoolean executed = new AtomicBoolean();
        event.addListener(_ -> executed.set(true));

        event.trigger();

        Assertions.assertTrue(executed.get());
    }

    @Test
    public void testTriggerWithArg()
    {
        Event<String> event = new SimpleEvent<>();
        AtomicReference<String> reference = new AtomicReference<>();

        event.addListener(reference::set);
        event.trigger("Hello, World!");

        Assertions.assertEquals("Hello, World!", reference.get());
    }

    @Test
    public void testListenerRemoval()
    {
        Event<Void> event = new SimpleEvent<>();
        AtomicBoolean executed = new AtomicBoolean();
        Consumer<Void> listener = _ -> executed.set(true);

        event.addListener(listener);
        event.trigger();

        Assertions.assertTrue(executed.get());

        executed.set(false);

        event.removeListener(listener);
        event.trigger();

        Assertions.assertFalse(executed.get());
    }

    @Test
    public void testAddListenerNull()
    {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> new SimpleEvent<>().addListener(null));
    }

    @Test
    public void testAddAllListenersArrayNull()
    {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> new SimpleEvent<>().addAllListeners((Consumer<Object>[]) null));
    }

    @Test
    public void testAddAllListenersArray()
    {
        Event<Void> event = new SimpleEvent<>();
        AtomicInteger counter = new AtomicInteger();

        event.addAllListeners(_ -> counter.incrementAndGet(), _ -> counter.incrementAndGet(), _ -> counter.incrementAndGet());

        event.trigger();

        Assertions.assertEquals(3, counter.get());
    }

    @Test
    public void testAddAllListenersArrayNullContained()
    {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> new SimpleEvent<>().addAllListeners(_ ->
        {
        }, null, _ ->
        {
        }));
    }

    @Test
    public void testAddAllListenersCollection()
    {
        Event<Void> event = new SimpleEvent<>();
        AtomicInteger counter = new AtomicInteger();

        event.addAllListeners(Arrays.asList(_ -> counter.incrementAndGet(), _ -> counter.incrementAndGet(), _ -> counter.incrementAndGet()));

        event.trigger();

        Assertions.assertEquals(3, counter.get());
    }

    @Test
    public void testAddAllListenersCollectionNull()
    {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> new SimpleEvent<>().addAllListeners((Collection<Consumer<Object>>) null));
    }

    @Test
    public void testAddAllListenersCollectionNullContained()
    {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> new SimpleEvent<>().addAllListeners(Arrays.asList(_ ->
        {
        }, null, _ ->
        {
        })));
    }

    @Test
    public void testDuplicateListener()
    {
        Event<Void> event = new SimpleEvent<>();
        AtomicInteger counter = new AtomicInteger();
        Consumer<Void> listener = _ -> counter.incrementAndGet();

        event.addListener(listener);
        event.addListener(listener);

        event.trigger();

        Assertions.assertEquals(2, counter.get());
    }

    @Test
    public void testRemoveListenerNull()
    {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> new SimpleEvent<>().removeListener(null));
    }

    @Test
    public void testRemoveAllOccurrences()
    {
        Event<Void> event = new SimpleEvent<>();
        AtomicInteger counter = new AtomicInteger();
        Consumer<Void> listener = _ -> counter.incrementAndGet();

        event.addAllListeners(listener, listener, listener);
        event.removeAllOccurrences(listener);

        event.trigger();

        Assertions.assertEquals(0, counter.get());
    }
}