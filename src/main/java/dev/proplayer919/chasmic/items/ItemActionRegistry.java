package dev.proplayer919.chasmic.items;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.items.actions.ItemWarpAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.network.packet.server.play.SetCooldownPacket;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ItemActionRegistry {
    private final Map<String, ItemAction> itemActions = new HashMap<>();

    public ItemActionRegistry() {
        // Register items actions here
        registerItemAction(new ItemAction("warp", 0, 20, new ItemWarpAction()));
    }

    public void registerItemAction(ItemAction itemAction) {
        itemActions.put(itemAction.id(), itemAction);
    }

    public ItemAction getItemAction(String id) {
        return itemActions.get(id);
    }
}
