package dev.proplayer919.chasmic;

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

import java.util.List;

@Getter
public class CustomPlayer extends Player {
    private PlayerRank rank = PlayerRank.DEFAULT;

    @Setter
    private PlayerData playerData;

    private final PermissionHolder permissionHolder = new PermissionHolder();

    @Setter
    private boolean recording = false;

    @Setter
    private boolean streaming = false;

    public CustomPlayer(PlayerConnection playerConnection, GameProfile gameProfile) {
        super(playerConnection, gameProfile);
        loadRankPermissions();
        updatePermissionLevel();
    }

    public CustomPlayer(PlayerConnection playerConnection, GameProfile gameProfile, PlayerRank rank) {
        this(playerConnection, gameProfile);
        this.rank = rank;
        loadRankPermissions();
        updatePermissionLevel();
    }

    public void setRank(PlayerRank rank) {
        this.rank = rank;
        loadRankPermissions();
        updateTabList();
        updatePermissionLevel();
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
        int tabListOrder = 1000 - (rank.getPriority() * 100);
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
}
