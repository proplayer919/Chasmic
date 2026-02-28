package dev.proplayer919.chasmic.items;

import dev.proplayer919.chasmic.CustomPlayer;

public interface ItemActionHandler {
    ItemActionResult handleAction(CustomPlayer customPlayer);
}
