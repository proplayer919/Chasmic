package dev.proplayer919.chasmic.service;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jspecify.annotations.NonNull;

/**
 * Base interface for all modules that can be attached to event nodes
 */
public interface Module {
    /**
     * Attaches this module's event listeners to the given event node
     * @param eventNode The event node to attach to
     */
    void attach(@NonNull EventNode<Event> eventNode);

    /**
     * Gets the name of this module
     * @return The module name
     */
    String getName();
}

