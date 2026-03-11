package dev.proplayer919.chasmic.service.module;

import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.service.Module;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.tag.Tag;
import org.jspecify.annotations.NonNull;

/**
 * Module that handles tab list updates when players spawn
 */
public class TabListModule implements Module {
    private static final Tag<Boolean> NEEDS_TAB_LIST_UPDATE = Tag.Boolean("needsTabListUpdate").defaultValue(false);

    @Override
    public void attach(@NonNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            CustomPlayer player = (CustomPlayer) event.getPlayer();
            // Mark that we need to update tab list on first tick after spawn
            player.setTag(NEEDS_TAB_LIST_UPDATE, true);
        });

        eventNode.addListener(PlayerTickEvent.class, event -> {
            CustomPlayer player = (CustomPlayer) event.getPlayer();
            // Update tab list on the first tick after spawn when player is in PLAY state
            if (player.hasTag(NEEDS_TAB_LIST_UPDATE)) {
                player.removeTag(NEEDS_TAB_LIST_UPDATE);
                player.updateTabList();
            }
        });
    }

    @Override
    public String getName() {
        return "TabListModule";
    }
}

