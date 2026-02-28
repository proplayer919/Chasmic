package dev.proplayer919.chasmic;

import dev.proplayer919.chasmic.data.MongoDBHandler;
import dev.proplayer919.chasmic.data.PlayerData;
import dev.proplayer919.chasmic.permission.PermissionHolder;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.tag.Tag;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class CustomPlayer extends Player {
    private PlayerRank rank = PlayerRank.DEFAULT;

    @Setter
    private PlayerData playerData;

    @Setter
    private int customHealth = 100;

    @Setter
    private int customMana = 100;

    private final Map<String, Date> itemCooldowns = new HashMap<>();

    private final PermissionHolder permissionHolder = new PermissionHolder();
    private boolean permissionsLoaded = false;

    @Setter
    private boolean recording = false;

    @Setter
    private boolean streaming = false;

    public static final Tag<Long> LAST_ATTACKED_TICKS = Tag.Long("lastAttackedTicks");

    public CustomPlayer(PlayerConnection playerConnection, GameProfile gameProfile) {
        super(playerConnection, gameProfile);
        updatePermissionLevel();
    }

    public CustomPlayer(PlayerConnection playerConnection, GameProfile gameProfile, PlayerRank rank) {
        this(playerConnection, gameProfile);
        this.rank = rank;
        updatePermissionLevel();
    }

    public void setRank(PlayerRank rank) {
        this.rank = rank;
        loadRankPermissions();
        updateTabList();
        updatePermissionLevel();
    }

    public void usedItem(String itemId) {
        itemCooldowns.put(itemId, new Date());
    }

    /**
     * Checks if player data has been loaded, which indicates that the player has fully initialized and is ready for permission checks and other operations that depend on player data.
     */
    public boolean isInitialized() {
        return playerData != null && permissionsLoaded;
    }

    /**
     * Loads permissions from the player's rank
     */
    private void loadRankPermissions() {
        permissionHolder.clearPermissions();

        // Load rank permissions
        for (String permission : rank.getDefaultPermissions()) {
            permissionHolder.addPermission(permission);
        }

        // Load custom permissions if player data is available
        if (playerData != null && playerData.getCustomPermissions() != null) {
            for (String permString : playerData.getCustomPermissions()) {
                if (permString.startsWith("-")) {
                    permissionHolder.addPermission(permString.substring(1), false);
                } else {
                    permissionHolder.addPermission(permString, true);
                }
            }
        }

        permissionsLoaded = true;
    }

    /**
     * Reloads all permissions from rank and player data
     */
    public void reloadPermissions() {
        loadRankPermissions();
    }

    /**
     * Checks if player has a permission
     */
    public boolean hasPermission(String permission) {
        return permissionHolder.hasPermission(permission);
    }

    /**
     * Adds a custom permission to this player
     */
    public void addPermission(String permission) {
        permissionHolder.addPermission(permission);

        // Update player data
        if (playerData != null) {
            if (!playerData.getCustomPermissions().contains(permission)) {
                playerData.getCustomPermissions().add(permission);
            }
        }
    }

    /**
     * Removes a custom permission from this player
     */
    public void removePermission(String permission) {
        permissionHolder.removePermission(permission);

        // Update player data
        if (playerData != null) {
            playerData.getCustomPermissions().remove(permission);
        }
    }

    public Component buildDisplayName() {
        Component displayName = Component.empty();

        // Add media status indicators
        if (recording) {
            displayName = displayName.append(Component.text("● ", NamedTextColor.RED));
        }
        if (streaming) {
            displayName = displayName.append(Component.text("● ", NamedTextColor.LIGHT_PURPLE));
        }

        // Add rank and username
        if (rank.getName() != null) {
            displayName = displayName.append(rank.getName())
                    .append(Component.text(" " + getUsername()).color(rank.getUsernameColor()));
        } else {
            displayName = displayName.append(Component.text(getUsername()));
        }

        return displayName;
    }

    public Component buildMessage(String message) {
        return buildDisplayName().append(Component.text(": ").color(NamedTextColor.WHITE)).append(Component.text(message).color(rank.getMessageColor()));
    }

    public void updatePermissionLevel() {
        if (rank == PlayerRank.ADMIN || rank == PlayerRank.OWNER) {
            setPermissionLevel(4);
        }
    }

    public void updateTabList() {
        // Only send tab list updates if player is in PLAY state
        if (!isOnline()) {
            return;
        }

        sendPacketToViewersAndSelf(new PlayerInfoUpdatePacket(
                PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                new PlayerInfoUpdatePacket.Entry(
                        getUuid(),
                        getUsername(),
                        List.of(),
                        false,
                        0,
                        getGameMode(),
                        buildDisplayName(),
                        null,
                        0,
                        false
                )
        ));

        // Update list order based on priority (higher priority = lower order number for sorting at top)
        // Minestom sorts tab list ascending, so we invert priority
        int tabListOrder = rank.getPriority() * 100;
        sendPacketToViewersAndSelf(new PlayerInfoUpdatePacket(
                PlayerInfoUpdatePacket.Action.UPDATE_LIST_ORDER,
                new PlayerInfoUpdatePacket.Entry(
                        getUuid(),
                        getUsername(),
                        List.of(),
                        false,
                        0,
                        getGameMode(),
                        null,
                        null,
                        tabListOrder,
                        false
                )
        ));
    }

    public void setMaxHealth(int maxHealth) {
        if (customHealth > maxHealth) {
            customHealth = maxHealth;
        }

        // Save to database
        MongoDBHandler mongoDBHandler = Main.getMongoDBHandler();
        if (playerData != null && mongoDBHandler != null) {
            playerData.setMaxHealth(maxHealth);
            mongoDBHandler.savePlayerData(playerData);
        }
    }

    public void setMaxMana(int maxMana) {
        if (customMana > maxMana) {
            customMana = maxMana;
        }

        // Save to database
        MongoDBHandler mongoDBHandler = Main.getMongoDBHandler();
        if (playerData != null && mongoDBHandler != null) {
            playerData.setMaxMana(maxMana);
            mongoDBHandler.savePlayerData(playerData);
        }
    }

    @Override
    public void tick(long time) {
        super.tick(time);

        // Handle mana and health regeneration
        if (isOnline()) {
            // Regenerate mana
            if (customMana < playerData.getMaxMana()) {
                customMana = Math.min(customMana + 1, playerData.getMaxMana());
            }

            // Regenerate health
            if (customHealth < playerData.getMaxHealth()) {
                customHealth = Math.min(customHealth + 1, playerData.getMaxHealth());
            }

            // Show action bar with health and mana
            Component healthDisplay = Component.text("❤ " + customHealth + "/" + playerData.getMaxHealth()).color(NamedTextColor.RED);
            Component manaDisplay = Component.text("✦ " + customMana + "/" + playerData.getMaxMana()).color(NamedTextColor.AQUA);
            sendActionBar(healthDisplay.append(Component.text("   ")).append(manaDisplay));
        }
    }
}
