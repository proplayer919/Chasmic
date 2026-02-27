package dev.proplayer919.chasmic.module;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;

/**
 * Base interface for all modules that can be attached to event nodes
 */
public interface Module {
    /**
     * Attaches this module's event listeners to the given event node
     * @param eventNode The event node to attach to
     */
    void attach(@NotNull EventNode<Event> eventNode);

    /**
     * Gets the name of this module
     * @return The module name
     */
    String getName();
}

