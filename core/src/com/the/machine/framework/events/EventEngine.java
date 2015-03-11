package com.the.machine.framework.events;

import java.util.*;

/**
 * TODO Add description.
 *
 * @author Fabian <f.fraenz@t-online.de>
 * @version 1.0
 * @since 29.08.2014
 */
public class EventEngine {

    protected Map<Class<? extends Event>, List<EventListener>> eventMap = new HashMap<>();
    protected Queue<Event> eventQueue = new ArrayDeque<>();

    /**
     * Registers given EventListener for the specified events.
     * @param registrant The EventListener.
     * @param events The events the listener should get registered too.
     */
    @SafeVarargs
    public final void register(EventListener registrant, Class<? extends Event>... events) {
        for (Class<? extends Event> eventClass : events) {
            List<EventListener> listeners = eventMap.get(eventClass);
            // check if there are already listeners for that event class, if not create the list
            if (listeners == null) {
                listeners = new ArrayList<EventListener>();
                eventMap.put(eventClass, listeners);
            }
            listeners.add(registrant);
        }
    }

    /**
     * Unregisters the EventListener for all events.
     * @param unregistrant The EventListener.
     */
    public void unregister(EventListener unregistrant) {
        for (Class<? extends Event> eventClass : eventMap.keySet()) {
            List<EventListener> listeners = eventMap.get(eventClass);
            listeners.remove(unregistrant);
        }
    }

    /**
     * Unregisters the EventListener for all specified events.
     * @param unregistrant The EventListener.
     * @param events The events the listener should unregister.
     */
    @SafeVarargs
    public final void unregister(EventListener unregistrant, Class<? extends Event> ... events) {
        for (Class<? extends Event> eventClass : events) {
            List<EventListener> listeners = eventMap.get(eventClass);
            // check if there are listeners for that event class
            if (listeners != null) {
                listeners.remove(unregistrant);
            }
        }
    }

    /**
     * Adds an event to the queue.
     * @param event The event.
     */
    public void dispatchEvent(Event event) {
        eventQueue.add(event);
    }

    /**
     * Let the listeners handle all events in the queue.
     */
    public void update() {
        // runs as long as there are events in the queue
        while (!eventQueue.isEmpty()) {
            // get next event from queue
            Event event = eventQueue.remove();
            List<EventListener> listeners = eventMap.get(event.getClass());
            if (listeners != null) {
                // forward events to there designated listeners
                for (EventListener listener : listeners) {
                    listener.handleEvent(event);
                    // check if the previous listener wants that the event is dropped. Therefore the event is not handled by following listeners.
                    if (event.isDrop()) {
                        return;
                    }
                }
            }
        }
    }

    public void reset() {
        eventQueue.clear();
        eventMap.clear();
    }

}
