package dev.proplayer919.chasmic.module;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager for handling multiple modules
 */
public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    /**
     * Registers a module to be managed
     * @param module The module to register
     * @return This manager for chaining
     */
    public ModuleManager register(@NotNull Module module) {
        modules.add(module);
        return this;
    }

    /**
     * Attaches all registered modules to the given event node
     * @param eventNode The event node to attach modules to
     */
    public void attachAll(@NotNull EventNode<Event> eventNode) {
        for (Module module : modules) {
            module.attach(eventNode);
        }
    }

    /**
     * Gets all registered modules
     * @return List of registered modules
     */
    public List<Module> getModules() {
        return new ArrayList<>(modules);
    }
}

