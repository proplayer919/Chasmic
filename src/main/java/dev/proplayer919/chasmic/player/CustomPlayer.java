package dev.proplayer919.chasmic.player;

import dev.proplayer919.chasmic.*;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import dev.proplayer919.chasmic.data.PlayerData;
import dev.proplayer919.chasmic.entities.CustomCreature;
import dev.proplayer919.chasmic.entities.HealthCreature;
import dev.proplayer919.chasmic.events.PlayerEnterLocationEvent;
import dev.proplayer919.chasmic.events.PlayerExitLocationEvent;
import dev.proplayer919.chasmic.helpers.CurrencyFormatter;
import dev.proplayer919.chasmic.helpers.ExpValue;
import dev.proplayer919.chasmic.location.Location;
import dev.proplayer919.chasmic.player.permission.PermissionHolder;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.event.player.PlayerRespawnEvent;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.network.packet.server.play.DamageEventPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.timer.TaskSchedule;
import org.jspecify.annotations.Nullable;

import java.util.*;

@Getter
public class CustomPlayer extends Player implements HealthCreature {
    private static final long HEALTH_REGEN_BASE_MILLIS = 50000;
    private static final long MANA_REGEN_BASE_MILLIS = 25000;
    private static final long MIN_REGEN_INTERVAL_MILLIS = 10;
    private static final long COMBAT_REGEN_DELAY_MILLIS = 5000;

    private PlayerRank rank = PlayerRank.DEFAULT;

    @Setter
    private PlayerData playerData;

    @Setter
    private ExpValue expValue;

    private final Map<String, Date> itemCooldowns = new HashMap<>();

    private final PermissionHolder permissionHolder = new PermissionHolder();
    private boolean permissionsLoaded = false;

    private boolean recording = false;
    private boolean streaming = false;

    private Date lastDamageTime;
    private String lastDamageCause;

    private Location currentLocation;

    // Managers for separated concerns
    @Getter
    private final PlayerStatsManager statsManager;

    @Getter
    private final PlayerUIManager uiManager;

    @Getter
    private final PlayerProfileInventoryManager profileInventoryManager;

    public void setRecording(boolean recording) {
        this.recording = recording;
        uiManager.markActionBarDirty();
    }

    public void setStreaming(boolean streaming) {
        this.streaming = streaming;
        uiManager.markActionBarDirty();
    }

    public CustomPlayer(PlayerConnection playerConnection, GameProfile gameProfile) {
        super(playerConnection, gameProfile);
        this.statsManager = new PlayerStatsManager(this);
        this.uiManager = new PlayerUIManager(this);
        this.profileInventoryManager = new PlayerProfileInventoryManager(this);
        updatePermissionLevel();
        setupRegenSchedule();
        setupPlayerAttributes();
        setupEventListeners();
    }

    public CustomPlayer(PlayerConnection playerConnection, GameProfile gameProfile, PlayerRank rank) {
        this(playerConnection, gameProfile);
        this.rank = rank;
    }

    public int getMaxCustomHealth() {
        return statsManager.getMaxCustomHealth();
    }

    public int getMaxCustomMana() {
        return statsManager.getMaxCustomMana();
    }

    public int getCustomHealth() {
        return statsManager.getCustomHealth();
    }

    public void setCustomHealth(int health) {
        statsManager.setCustomHealth(health);
        uiManager.markActionBarDirty();
    }

    public int getCustomMana() {
        return statsManager.getCustomMana();
    }

    public void setCustomMana(int mana) {
        statsManager.setCustomMana(mana);
        uiManager.markActionBarDirty();
    }

    public void dataLoadCallback() {
        // Called after player data is loaded to set initial health/mana
        if (playerData != null) {
            refreshFromActiveProfile();
            profileInventoryManager.loadActiveProfileInventory();

            // Initialize UI manager after player data is loaded
            uiManager.initializeSidebar();
        }
    }

    public void refreshFromActiveProfile() {
        if (playerData == null) {
            return;
        }

        this.expValue = new ExpValue(playerData.getCurrentExp());
        this.statsManager.markAllStatsDirty();
        markSpeedStatDirty();
        uiManager.markActionBarDirty();
    }

    public boolean switchToProfile(String profileId) {
        if (playerData == null) {
            return false;
        }

        profileInventoryManager.saveActiveProfileInventory();

        if (!playerData.switchActiveProfile(profileId)) {
            return false;
        }

        profileInventoryManager.loadActiveProfileInventory();
        refreshFromActiveProfile();
        return true;
    }

    public boolean saveActiveProfileInventory() {
        return profileInventoryManager.saveActiveProfileInventory();
    }

    public void loadActiveProfileInventory() {
        profileInventoryManager.loadActiveProfileInventory();
    }

    public void persistActiveProfileInventoryIfChanged() {
        profileInventoryManager.persistActiveProfileInventoryIfChanged();
    }

    private static final int ATTACK_SPEED_BONUS = 1000;

    private void setupPlayerAttributes() {
        getAttribute(Attribute.ATTACK_SPEED).addModifier(new AttributeModifier(UUID.randomUUID().toString(), ATTACK_SPEED_BONUS, AttributeOperation.ADD_VALUE));
    }

    private void setupEventListeners() {
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(PlayerDeathEvent.class, event -> {
            if (event.getPlayer().getUuid().equals(getUuid())) {
                event.setChatMessage(null);

                String deathCause = lastDamageCause != null ? lastDamageCause : "the environment";
                event.setDeathText(Component.text("☠  Killed by " + deathCause).color(NamedTextColor.RED));

                broadcastDeathMessage(deathCause);
                applyDeathPursePenalty();
                lastDamageCause = null;
            }
        });

        globalEventHandler.addListener(PlayerRespawnEvent.class, event -> {
            if (event.getPlayer().getUuid().equals(getUuid())) {
                // Reset health and mana on respawn
                statsManager.setCustomHealth(playerData != null ? (int) statsManager.getStatFor(PlayerStat.HEALTH) : 100);
                statsManager.setCustomMana(playerData != null ? (int) statsManager.getStatFor(PlayerStat.INTELLIGENCE) : 100);
                uiManager.markActionBarDirty();
            }
        });

        globalEventHandler.addListener(ServerTickMonitorEvent.class, event -> uiManager.updateMspt((float) event.getTickMonitor().getTickTime()));
    }

    private void setupRegenSchedule() {
        // Schedule health regeneration with interval dependent on max health
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            // Only regenerate if player is online, has player data loaded, and it's been at least 5 seconds since last damage
            if (isOnline() && playerData != null && (lastDamageTime == null || new Date().getTime() - lastDamageTime.getTime() >= COMBAT_REGEN_DELAY_MILLIS)) {
                // Regenerate health
                if (statsManager.getCustomHealth() < statsManager.getStatFor(PlayerStat.HEALTH)) {
                    statsManager.setCustomHealth((int) Math.min(statsManager.getCustomHealth() + 1, statsManager.getStatFor(PlayerStat.HEALTH)));
                    uiManager.markActionBarDirty();
                }
            }

            // Calculate interval: 50000 / maxHealth milliseconds (for 100 max = 500ms)
            if (playerData != null) {
                long healthInterval = (long) Math.max(HEALTH_REGEN_BASE_MILLIS / statsManager.getStatFor(PlayerStat.HEALTH), MIN_REGEN_INTERVAL_MILLIS);
                return TaskSchedule.millis(healthInterval);
            }
            return TaskSchedule.millis(500);
        });

        // Schedule mana regeneration with interval dependent on max mana
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (isOnline() && playerData != null) {
                // Regenerate mana
                if (statsManager.getCustomMana() < statsManager.getStatFor(PlayerStat.INTELLIGENCE)) {
                    statsManager.setCustomMana((int) Math.min(statsManager.getCustomMana() + 1, statsManager.getStatFor(PlayerStat.INTELLIGENCE)));
                    uiManager.markActionBarDirty();
                }
            }

            // Calculate interval: 25000 / maxMana milliseconds (for 100 max = 250ms)
            if (playerData != null) {
                long manaInterval = (long) Math.max(MANA_REGEN_BASE_MILLIS / statsManager.getStatFor(PlayerStat.INTELLIGENCE), MIN_REGEN_INTERVAL_MILLIS);
                return TaskSchedule.millis(manaInterval);
            }
            return TaskSchedule.millis(250);
        });
    }

    @Override
    public void damage(int amount, RegistryKey<DamageType> damageType, @Nullable Entity attacker, Pos damageSourcePos) {
        if (getGameMode() == GameMode.CREATIVE || getGameMode() == GameMode.SPECTATOR) {
            return; // No damage in creative or spectator mode
        }

        lastDamageTime = new Date();

        if (attacker instanceof CustomCreature creature) {
            lastDamageCause = formatNameFromEnum(creature.getCreatureType().name());
        } else {
            lastDamageCause = "the environment";
        }

        statsManager.setCustomHealth(Math.max(0, statsManager.getCustomHealth() - amount));
        uiManager.markActionBarDirty();

        // Send a damage packet to animate a damage tick
        if (attacker == null) {
            attacker = this; // Use self as attacker if null to prevent issues with null references in the packet
        }

        DamageEventPacket damagePacket = new DamageEventPacket(getEntityId(), new Damage(damageType, attacker, attacker, damageSourcePos, 0.1f).getTypeId(), attacker.getEntityId(), attacker.getEntityId(), damageSourcePos);
        sendPacketToViewersAndSelf(damagePacket);

        if (statsManager.getCustomHealth() == 0) {
            this.kill();
        }
    }

    private void broadcastDeathMessage(String deathCause) {
        Component message = Component.text(Emojis.SKULL.getEmoji() + " ", NamedTextColor.RED)
                .append(buildDisplayName())
                .append(Component.text(" was killed by " + deathCause + ".", NamedTextColor.RED));

        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            onlinePlayer.sendMessage(message);
        }
    }

    private void applyDeathPursePenalty() {
        if (playerData == null) {
            return;
        }

        long previousPurse = Math.max(0, playerData.getPurse());
        long cappedHalfLoss = Math.min(previousPurse / 2, 100_000L);
        long lostAmount = previousPurse > 0 ? Math.max(1L, cappedHalfLoss) : 0L;
        lostAmount = Math.min(lostAmount, previousPurse);
        long remainingAmount = previousPurse - lostAmount;
        playerData.setPurse(remainingAmount);
        uiManager.markActionBarDirty();

        MongoDBHandler mongoDBHandler = Main.getServiceContainer() != null
                ? Main.getServiceContainer().getMongoDBHandler()
                : null;
        if (mongoDBHandler != null) {
            mongoDBHandler.savePlayerData(playerData);
        }

        if (playerData.isDeathPurseNoticeAcknowledged()) {
            return;
        }

        Component acknowledge = Component.text("[I GET IT]", NamedTextColor.GREEN)
                .clickEvent(ClickEvent.runCommand("/deathnoticeack"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to hide this reminder next time.", NamedTextColor.YELLOW)));

        sendMessage(Component.text("Because you died, ", NamedTextColor.RED)
                .append(Component.text(CurrencyFormatter.formatCurrency(lostAmount), NamedTextColor.GOLD))
                .append(Component.text(" pennies were removed from your purse. Death penalty = half your purse, up to 100k max (and at least 1 coin if you had any). Remaining purse: ", NamedTextColor.RED))
                .append(Component.text(CurrencyFormatter.formatCurrency(remainingAmount), NamedTextColor.GOLD))
                .append(Component.text(". ", NamedTextColor.RED))
                .append(acknowledge));
    }

    private String formatNameFromEnum(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return "Unknown";
        }

        StringBuilder builder = new StringBuilder();
        String[] parts = rawName.toLowerCase(Locale.ROOT).split("_");
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }

            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.isEmpty() ? rawName : builder.toString();
    }

    public void addTemporaryStatBonus(PlayerStat stat, float bonusAmount, long durationMillis) {
        // Create callback that includes marking speed stat dirty on expiry
        Runnable onExpire = () -> {
            sendMessage(Component.text("Your " + stat.name().toLowerCase().replace("_", " ") + " bonus has expired!").color(NamedTextColor.YELLOW));
            // Mark speed stat dirty if this was a speed bonus
            if (stat == PlayerStat.SPEED) {
                markSpeedStatDirty();
            }
        };

        statsManager.addTemporaryStatBonus(stat, bonusAmount, durationMillis, onExpire);

        // Mark speed stat dirty when bonus is added
        if (stat == PlayerStat.SPEED) {
            markSpeedStatDirty();
        }
    }

    public void unlockLocation(Location location) {
        if (playerData != null && !playerData.getUnlockedLocationIds().contains(location.id())) {
            playerData.getUnlockedLocationIds().add(location.id());

            // Save to database
            MongoDBHandler mongoDBHandler = Main.getServiceContainer().getMongoDBHandler();
            if (mongoDBHandler != null) {
                mongoDBHandler.savePlayerData(playerData);
            }
        }
    }

    public void setCurrentLocation(Location location) {
        Location previousLocation = this.currentLocation;

        // Fire exit event before state change
        if (previousLocation != null) {
            MinecraftServer.getGlobalEventHandler().call(new PlayerExitLocationEvent(this, previousLocation));
        }

        this.currentLocation = location;

        // Fire enter event after state change
        if (location != null) {
            MinecraftServer.getGlobalEventHandler().call(new PlayerEnterLocationEvent(this, location));
        }
    }

    @Override
    public float getAttackStat() {
        return statsManager.getStatFor(PlayerStat.ATTACK);
    }

    @Override
    public float getDefenseStat() {
        return statsManager.getStatFor(PlayerStat.DEFENSE);
    }

    @Override
    public float getCriticalChanceStat() {
        return statsManager.getStatFor(PlayerStat.CRITICAL_CHANCE);
    }

    @Override
    public float getSpeedStat() {
        return statsManager.getStatFor(PlayerStat.SPEED);
    }

    public void showSidebar() {
        uiManager.showSidebar();
    }

    // Cached speed stat with dirty flag
    private float cachedSpeedStat = 0f;
    private boolean speedStatDirty = true;

    @Override
    public void tick(long time) {
        super.tick(time);

        if (isOnline() && playerData != null) {
            // Delegate UI updates to UI manager (already throttled)
            uiManager.tick(time);

            // Update movement speed based on speed stat (cached, only recalculate when dirty)
            if (speedStatDirty) {
                cachedSpeedStat = getSpeedStat();
                speedStatDirty = false;
            }

            if (cachedSpeedStat != 0) {
                float calculatedSpeed = 0.1f * (1 + cachedSpeedStat / 100);
                getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(calculatedSpeed);
            } else {
                getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1f);
            }
        }
    }

    /**
     * Mark speed stat as dirty to trigger recalculation on next tick
     */
    public void markSpeedStatDirty() {
        speedStatDirty = true;
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

        // Load inherited rank permissions (this rank + all lower priorities)
        for (String permission : rank.getInheritedDefaultPermissions()) {
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
}

