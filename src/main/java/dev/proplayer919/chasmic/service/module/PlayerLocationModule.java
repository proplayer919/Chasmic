package dev.proplayer919.chasmic.service.module;

import dev.proplayer919.chasmic.events.PlayerEnterLocationEvent;
import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.service.Module;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jspecify.annotations.NonNull;

/**
 * Module that handles player location unlock
 */
public class PlayerLocationModule implements Module {

    @Override
    public void attach(@NonNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerEnterLocationEvent.class, event -> {
            CustomPlayer player = (CustomPlayer) event.getPlayer();

            // Ensure player data is loaded
            if (player.getPlayerData() == null) {
                return;
            }

            if (player.getPlayerData().getUnlockedLocationIds().contains(event.getLocation().id())) {
                // Player has already unlocked this location, do nothing
                return;
            }

            // Unlock the location for the player
            player.unlockLocation(event.getLocation());

            // Play a sound
            player.playSound(Sound.sound(Key.key("entity.player.levelup"), Sound.Source.PLAYER, 1f, 1f));

            // Show a title
            Component title = Component.text("Location Unlocked!").color(NamedTextColor.GREEN);
            Component subtitle = Component.text(event.getLocation().name()).color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD);
            player.showTitle(Title.title(title, subtitle));
        });
    }

    @Override
    public String getName() {
        return "PlayerLocationModule";
    }
}
