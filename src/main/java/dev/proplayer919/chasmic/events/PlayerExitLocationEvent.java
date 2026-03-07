package dev.proplayer919.chasmic.events;

import dev.proplayer919.chasmic.location.Location;
import lombok.Getter;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;

@Getter
public class PlayerExitLocationEvent implements PlayerEvent {
    private final Player player;
    private final Location location;

    public PlayerExitLocationEvent(Player player, Location location) {
        this.player = player;
        this.location = location;
    }
}
