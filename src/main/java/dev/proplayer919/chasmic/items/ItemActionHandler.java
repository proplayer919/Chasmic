package dev.proplayer919.chasmic.items;

import dev.proplayer919.chasmic.player.CustomPlayer;

public interface ItemActionHandler {
    ItemActionResult handleAction(CustomPlayer customPlayer);
}
