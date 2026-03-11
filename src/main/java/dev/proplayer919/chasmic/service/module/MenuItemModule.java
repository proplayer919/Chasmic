package dev.proplayer919.chasmic.service.module;


import dev.proplayer919.chasmic.PlayerHeads;
import dev.proplayer919.chasmic.Rarity;
import dev.proplayer919.chasmic.accessories.AccessoryRegistry;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import dev.proplayer919.chasmic.data.PlayerData;
import dev.proplayer919.chasmic.data.PlayerProfileData;
import dev.proplayer919.chasmic.helpers.NumberFormatter;
import dev.proplayer919.chasmic.player.gui.GuiClickAction;
import dev.proplayer919.chasmic.player.gui.GuiClickActionType;
import dev.proplayer919.chasmic.player.gui.GuiItem;
import dev.proplayer919.chasmic.player.gui.GuiScreen;
import dev.proplayer919.chasmic.helpers.ItemCreator;
import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.service.Module;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerGameModeChangeEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuItemModule implements Module {
    private static final ItemStack MENU_ITEM = ItemCreator.createItem(1, Material.NETHER_STAR, "menu", "Chasmic Menu", "Use this to access the Chasmic Menu and manage your profile.", Rarity.SPECIAL, Collections.emptyList(), null, null, null, false);
    private static final int MENU_SLOT = 8;

    private final MongoDBHandler mongoDBHandler;
    private final AccessoryRegistry accessoryRegistry;

    public MenuItemModule(MongoDBHandler mongoDBHandler, AccessoryRegistry accessoryRegistry) {
        this.mongoDBHandler = mongoDBHandler;
        this.accessoryRegistry = accessoryRegistry;
    }

    @Override
    public void attach(@NonNull EventNode<Event> eventNode) {
        // Add menu items when player spawns
        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            Player player = event.getPlayer();
            if (player.getGameMode() == GameMode.SURVIVAL) {
                giveMenuItems(player);
            }
        });

        // Register a listener to check when a player switches to survival mode and add the menu item if they don't have it
        eventNode.addListener(PlayerGameModeChangeEvent.class, event -> {
            if (event.getNewGameMode() != GameMode.SURVIVAL) {
                return;
            }

            giveMenuItems(event.getPlayer());
        });

        // Register another listener to make sure the menu item isn't moved or replaced
        eventNode.addListener(InventoryPreClickEvent.class, event -> {
            if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
                return;
            }

            if (event.getInventory() instanceof PlayerInventory && event.getSlot() == MENU_SLOT) {
                event.setCancelled(true);
            }
        });

        // Register a listener for when the menu item is clicked to open the menu screen
        eventNode.addListener(PlayerUseItemEvent.class, event -> {
            if (event.getItemStack().equals(MENU_ITEM) && event.getPlayer() instanceof CustomPlayer player) {
                GuiScreen menuScreen = createMenuScreen(player);
                player.openInventory(menuScreen.getInventory(accessoryRegistry, mongoDBHandler));
                event.setCancelled(true);
            }
        });
    }

    @Override
    public String getName() {
        return "MenuItemModule";
    }

    private void giveMenuItems(Player player) {
        player.getInventory().setItemStack(MENU_SLOT, MENU_ITEM);
    }

    /**
     * Creates a fresh menu screen for the given player
     */
    private GuiScreen createMenuScreen(CustomPlayer player) {
        Map<Integer, GuiItem> menuItems = new HashMap<>();

        menuItems.put(10, new GuiItem(Material.PLAYER_HEAD,
                Component.text("Profiles").color(NamedTextColor.YELLOW),
                "Create, switch, and delete gameplay profiles.",
                new GuiClickAction(GuiClickActionType.CUSTOM, event -> event.getPlayer().openInventory(createProfileManagementScreen(player).getInventory(accessoryRegistry, mongoDBHandler)))));

        List<Component> expDescription = new ArrayList<>();
        expDescription.add(Component.text("Current Level: ").color(NamedTextColor.GRAY).append(Component.text(player.getExpValue().getLevel()).color(NamedTextColor.GREEN)).decoration(TextDecoration.ITALIC, false));
        expDescription.add(Component.text("Current EXP: ").color(NamedTextColor.GRAY).append(Component.text(NumberFormatter.formatExp(player.getExpValue().getExpToPreviousLevel())).color(NamedTextColor.AQUA).append(Component.text("/").color(NamedTextColor.DARK_GRAY).append(Component.text(NumberFormatter.formatExp(player.getExpValue().getExpToNextLevel())).color(NamedTextColor.GRAY)))).decoration(TextDecoration.ITALIC, false));

        menuItems.put(11, new GuiItem(Material.EXPERIENCE_BOTTLE, Component.text("Chasmic Level").color(NamedTextColor.GREEN), expDescription, null));

        // Add Accessory Bag button
        List<Component> accessoryBagDescription = new ArrayList<>();
        accessoryBagDescription.add(Component.text("Store and manage your accessories").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        accessoryBagDescription.add(Component.empty());
        accessoryBagDescription.add(Component.text("Slots: ").color(NamedTextColor.GRAY).append(Component.text(player.getPlayerData().getAccessorySlots()).color(NamedTextColor.GREEN)).decoration(TextDecoration.ITALIC, false));

        menuItems.put(12, ItemCreator.createGuiItemWithTexture(
                PlayerHeads.ACCESSORY_BAG,
                Component.text("Accessory Bag").color(NamedTextColor.LIGHT_PURPLE),
                accessoryBagDescription,
                new GuiClickAction(GuiClickActionType.OPEN_ACCESSORY_BAG)
        ));

        return new GuiScreen(
                Component.text("Chasmic Menu"),
                InventoryType.CHEST_4_ROW,
                menuItems
        );
    }

    private GuiScreen createProfileManagementScreen(CustomPlayer player) {
        PlayerData playerData = player.getPlayerData();
        playerData.ensureProfileIntegrity();

        int maxSlots = Math.max(1, player.getRank().getProfileSlots());
        InventoryType type = calculateInventoryType(maxSlots);
        Map<Integer, GuiItem> profileItems = new HashMap<>();

        for (int i = 0; i < maxSlots; i++) {
            int slotNumber = i + 1;
            String profileId = PlayerProfileData.buildIdForSlot(slotNumber);

            if (playerData.hasProfile(profileId)) {
                boolean active = profileId.equals(playerData.getActiveProfileId());
                ItemStack icon = buildProfileIcon(slotNumber, active);

                GuiClickAction leftAction = new GuiClickAction(GuiClickActionType.CUSTOM, event -> {
                    if (!active && player.switchToProfile(profileId)) {
                        savePlayerData(player);
                        player.sendMessage(Component.text("Switched to Profile " + slotNumber + ".", NamedTextColor.GREEN));
                    }
                    event.getPlayer().openInventory(createProfileManagementScreen(player).getInventory(accessoryRegistry, mongoDBHandler));
                });

                GuiClickAction rightAction = new GuiClickAction(GuiClickActionType.CUSTOM,
                        event -> event.getPlayer().openInventory(createDeleteProfileConfirmScreen(player, profileId, slotNumber).getInventory(accessoryRegistry, mongoDBHandler)));

                profileItems.put(i, new GuiItem(icon, leftAction, rightAction));
            } else {
                ItemStack icon = buildEmptySlotIcon(slotNumber);
                GuiClickAction leftAction = new GuiClickAction(GuiClickActionType.CUSTOM, event -> {
                    playerData.createProfileForSlot(slotNumber);
                    player.switchToProfile(profileId);
                    savePlayerData(player);
                    player.sendMessage(Component.text("Created and switched to Profile " + slotNumber + ".", NamedTextColor.GREEN));
                    event.getPlayer().openInventory(createProfileManagementScreen(player).getInventory(accessoryRegistry, mongoDBHandler));
                });
                profileItems.put(i, new GuiItem(icon, leftAction));
            }
        }

        profileItems.put(type.getSize() - 1, new GuiItem(Material.BARRIER,
                Component.text("Back").color(NamedTextColor.RED),
                "Return to Chasmic Menu.",
                new GuiClickAction(GuiClickActionType.CUSTOM, event -> event.getPlayer().openInventory(createMenuScreen(player).getInventory(accessoryRegistry, mongoDBHandler)))));

        return new GuiScreen(Component.text("Profiles"), type, profileItems);
    }

    private GuiScreen createDeleteProfileConfirmScreen(CustomPlayer player, String profileId, int slotNumber) {
        Map<Integer, GuiItem> items = new HashMap<>();

        items.put(3, new GuiItem(Material.LIME_DYE,
                Component.text("Cancel").color(NamedTextColor.GREEN),
                "Keep this profile and return.",
                new GuiClickAction(GuiClickActionType.CUSTOM, event -> event.getPlayer().openInventory(createProfileManagementScreen(player).getInventory(accessoryRegistry, mongoDBHandler)))));

        items.put(5, new GuiItem(Material.RED_DYE,
                Component.text("Delete Profile " + slotNumber).color(NamedTextColor.RED),
                "This permanently deletes this profile's data.",
                new GuiClickAction(GuiClickActionType.CUSTOM, event -> {
                    PlayerData playerData = player.getPlayerData();
                    if (playerData.getProfiles().size() <= 1) {
                        player.sendMessage(Component.text("You must keep at least one profile.", NamedTextColor.RED));
                        event.getPlayer().openInventory(createProfileManagementScreen(player).getInventory(accessoryRegistry, mongoDBHandler));
                        return;
                    }

                    boolean wasActive = profileId.equals(playerData.getActiveProfileId());
                    if (playerData.deleteProfile(profileId)) {
                        if (wasActive) {
                            player.loadActiveProfileInventory();
                        }
                        player.refreshFromActiveProfile();
                        savePlayerData(player);
                        player.sendMessage(Component.text("Profile " + slotNumber + " deleted.", NamedTextColor.YELLOW));
                    }

                    event.getPlayer().openInventory(createProfileManagementScreen(player).getInventory(accessoryRegistry, mongoDBHandler));
                })));

        return new GuiScreen(Component.text("Confirm Delete"), InventoryType.CHEST_1_ROW, items);
    }

    private ItemStack buildProfileIcon(int slotNumber, boolean active) {
        Material material = active ? Material.YELLOW_DYE : Material.LIME_DYE;
        List<Component> lore = new ArrayList<>();

        lore.add(Component.text(active ? "Active profile" : "Existing profile").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Left Click: ").color(NamedTextColor.GRAY)
                .append(Component.text(active ? "Already active" : "Switch to this profile").color(NamedTextColor.YELLOW))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Right Click: ").color(NamedTextColor.GRAY)
                .append(Component.text("Delete profile").color(NamedTextColor.RED))
                .decoration(TextDecoration.ITALIC, false));

        return ItemStack.builder(material)
                .set(DataComponents.ITEM_NAME, Component.text("Profile " + slotNumber).color(active ? NamedTextColor.YELLOW : NamedTextColor.GREEN))
                .lore(lore)
                .build();
    }

    private ItemStack buildEmptySlotIcon(int slotNumber) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("No profile created in this slot.").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Left Click: ").color(NamedTextColor.GRAY)
                .append(Component.text("Create and switch").color(NamedTextColor.GREEN))
                .decoration(TextDecoration.ITALIC, false));

        return ItemStack.builder(Material.LIGHT_GRAY_DYE)
                .set(DataComponents.ITEM_NAME, Component.text("Empty Slot " + slotNumber).color(NamedTextColor.GRAY))
                .lore(lore)
                .build();
    }

    private InventoryType calculateInventoryType(int slots) {
        if (slots <= 9) {
            return InventoryType.CHEST_1_ROW;
        } else if (slots <= 18) {
            return InventoryType.CHEST_2_ROW;
        } else if (slots <= 27) {
            return InventoryType.CHEST_3_ROW;
        } else if (slots <= 36) {
            return InventoryType.CHEST_4_ROW;
        } else if (slots <= 45) {
            return InventoryType.CHEST_5_ROW;
        }

        return InventoryType.CHEST_6_ROW;
    }

    private void savePlayerData(CustomPlayer player) {
        if (mongoDBHandler != null && player.getPlayerData() != null) {
            mongoDBHandler.savePlayerData(player.getPlayerData());
        }
    }
}
