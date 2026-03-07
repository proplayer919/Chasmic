package dev.proplayer919.chasmic.items;

import dev.proplayer919.chasmic.items.actions.ImmaterialBoostAction;
import dev.proplayer919.chasmic.items.actions.ShallowLeapAction;

import java.util.HashMap;
import java.util.Map;

public class ItemActionRegistry {
    private final Map<String, ItemAction> itemActions = new HashMap<>();

    public ItemActionRegistry() {
        // Register items actions here
        registerItemAction(new ItemAction("shallow_leap", "Shallow Leap", "Teleports the user 10 blocks forward by summoning the power of the Shallows.", 0, ItemActionType.RIGHT_CLICK, 35, new ShallowLeapAction()));
        registerItemAction(new ItemAction("immaterial_boost", "Immaterial Boost", "Grants the user a burst of speed for 15 seconds, allowing them to quickly traverse the environment.", 90, ItemActionType.SHIFT_RIGHT_CLICK, 50, new ImmaterialBoostAction()));
    }

    public void registerItemAction(ItemAction itemAction) {
        itemActions.put(itemAction.id(), itemAction);
    }

    public ItemAction getItemAction(String id) {
        return itemActions.get(id);
    }
}
