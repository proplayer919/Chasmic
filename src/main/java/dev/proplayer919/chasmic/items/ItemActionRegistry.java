package dev.proplayer919.chasmic.items;

import dev.proplayer919.chasmic.items.actions.ItemWarpAction;

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
