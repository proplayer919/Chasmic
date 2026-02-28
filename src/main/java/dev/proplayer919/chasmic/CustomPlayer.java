package dev.proplayer919.chasmic;

import dev.proplayer919.chasmic.combat.CombatUtils;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import dev.proplayer919.chasmic.data.PlayerData;
import dev.proplayer919.chasmic.entities.CustomCreature;
import dev.proplayer919.chasmic.entities.HealthCreature;
import dev.proplayer919.chasmic.items.CustomItem;
import dev.proplayer919.chasmic.permission.PermissionHolder;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.event.player.PlayerRespawnEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.timer.TaskSchedule;
import org.jspecify.annotations.NonNull;

import java.util.*;

@Getter
public class CustomPlayer extends Player implements HealthCreature {
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

    private Date lastDamageTime;
    private CustomCreature lastDamageAttacker;

    public CustomPlayer(PlayerConnection playerConnection, GameProfile gameProfile) {
        super(playerConnection, gameProfile);
        updatePermissionLevel();
        setupRegenSchedule();
        setupPlayerAttributes();
        setupEventListeners();
    }

    public CustomPlayer(PlayerConnection playerConnection, GameProfile gameProfile, PlayerRank rank) {
        this(playerConnection, gameProfile);
        this.rank = rank;
    }

    public void dataLoadCallback() {
        // Called after player data is loaded to set initial health/mana
        if (playerData != null) {
            this.customHealth = playerData.getMaxHealth();
            this.customMana = playerData.getMaxMana();
        }
    }

    private void setupPlayerAttributes() {
        getAttribute(Attribute.ATTACK_SPEED).addModifier(new AttributeModifier(UUID.randomUUID().toString(), 1000, AttributeOperation.ADD_VALUE));
    }

    private void setupEventListeners() {
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(PlayerDeathEvent.class, event -> {
            if (event.getPlayer().getUuid().equals(getUuid())) {
                event.setChatMessage(null);

                if (lastDamageAttacker != null) {
                    event.setDeathText(Component.text("☠  Killed by " + lastDamageAttacker.getName()).color(NamedTextColor.RED));
                    lastDamageAttacker = null;
                } else {
                    event.setDeathText(Component.text("☠ You died!").color(NamedTextColor.RED));
                }
            }
        });

        globalEventHandler.addListener(PlayerRespawnEvent.class, event -> {
            if (event.getPlayer().getUuid().equals(getUuid())) {
                // Reset health and mana on respawn
                customHealth = playerData != null ? playerData.getMaxHealth() : 100;
                customMana = playerData != null ? playerData.getMaxMana() : 100;
            }
        });
    }

    private void setupRegenSchedule() {
        // Schedule health regeneration with interval dependent on max health
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            // Only regenerate if player is online, has player data loaded, and it's been at least 5 seconds since last damage
            if (isOnline() && playerData != null && (lastDamageTime == null || new Date().getTime() - lastDamageTime.getTime() >= 5000)) {
                // Regenerate health
                if (customHealth < playerData.getMaxHealth()) {
                    customHealth = Math.min(customHealth + 1, playerData.getMaxHealth());
                }
            }

            // Calculate interval: 50000 / maxHealth milliseconds (for 100 max = 500ms)
            if (playerData != null) {
                long healthInterval = Math.max(50000 / playerData.getMaxHealth(), 10);
                return TaskSchedule.millis(healthInterval);
            }
            return TaskSchedule.millis(500);
        });

        // Schedule mana regeneration with interval dependent on max mana
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (isOnline() && playerData != null) {
                // Regenerate mana
                if (customMana < playerData.getMaxMana()) {
                    customMana = Math.min(customMana + 1, playerData.getMaxMana());
                }
            }

            // Calculate interval: 25000 / maxMana milliseconds (for 100 max = 250ms)
            if (playerData != null) {
                long manaInterval = Math.max(25000 / playerData.getMaxMana(), 10);
                return TaskSchedule.millis(manaInterval);
            }
            return TaskSchedule.millis(250);
        });
    }

    @Override
    public void damage(int amount, RegistryKey<DamageType> damageType, Entity attacker, Pos damageSourcePos) {
        if (getGameMode() == GameMode.CREATIVE || getGameMode() == GameMode.SPECTATOR) {
            return; // No damage in creative or spectator mode
        }

        lastDamageTime = new Date();

        if (attacker instanceof CustomCreature) {
            lastDamageAttacker = (CustomCreature) attacker;
        } else {
            lastDamageAttacker = null;
        }

        // Apply defense stat to reduce damage
        int finalDamage = CombatUtils.applyDefenseToIncomingDamage(amount, getDefenseStat());

        this.customHealth -= finalDamage;

        if (this.customHealth <= 0) {
            this.customHealth = 0;
            this.kill();
        }
    }

    private Collection<PlayerStatBonus> getMainHandItemStatBonuses() {
        ItemStack mainHandItem = getItemInMainHand();
        if (!mainHandItem.isAir()) {
            CustomItem customItem = CustomItem.getCustomItemFromItemStack(mainHandItem);
            if (customItem != null) {
                return customItem.getStatBonuses();
            }
        }
        return Collections.emptyList();
    }

    private float getStatFor(PlayerStat stat) {
        float baseValue = PlayerStatConstants.getBaseValue(stat);
        if (playerData == null) {
            return baseValue;
        }
        float bonus = playerData.getAccessoryBagObject().sumStats().getOrDefault(stat, 0f);

        Collection<PlayerStatBonus> mainHandBonuses = getMainHandItemStatBonuses();
        for (PlayerStatBonus statBonus : mainHandBonuses) {
            if (statBonus.stat() == stat) {
                bonus += statBonus.bonusAmount();
            }
        }

        return baseValue + bonus;
    }

    @Override
    public float getAttackStat() {
        return getStatFor(PlayerStat.ATTACK);
    }

    @Override
    public float getDefenseStat() {
        return getStatFor(PlayerStat.DEFENSE);
    }

    @Override
    public float getCriticalChanceStat() {
        return getStatFor(PlayerStat.CRITICAL_CHANCE);
    }

    public void tick(long time) {
        super.tick(time);

        // Only update action bar if values have changed (prevents massive lag from packet spam)
        if (isOnline() && playerData != null) {
            // Show action bar with health and mana
            Component actionBarContent = getActionBarContent();

            sendActionBar(actionBarContent);
        }
    }

    private @NonNull Component getActionBarContent() {
        Component healthDisplay = Component.text("❤ " + customHealth + "/" + playerData.getMaxHealth()).color(NamedTextColor.RED);
        Component manaDisplay = Component.text("✦ " + customMana + "/" + playerData.getMaxMana()).color(NamedTextColor.AQUA);
        Component actionBarContent = healthDisplay.append(Component.text("   ")).append(manaDisplay);

        // Add streaming/recording status if applicable
        if (streaming || recording) {
            actionBarContent = actionBarContent.append(Component.text("   |   ").color(NamedTextColor.GRAY));
            if (recording) {
                actionBarContent = actionBarContent.append(Component.text("● Recording").color(NamedTextColor.RED));
            }
            if (streaming) {
                if (recording) {
                    actionBarContent = actionBarContent.append(Component.text(" "));
                }
                actionBarContent = actionBarContent.append(Component.text("● Streaming").color(NamedTextColor.LIGHT_PURPLE));
            }
        }
        return actionBarContent;
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
}
