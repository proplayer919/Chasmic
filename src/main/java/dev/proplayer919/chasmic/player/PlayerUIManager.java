package dev.proplayer919.chasmic.player;

import dev.proplayer919.chasmic.Emojis;
import dev.proplayer919.chasmic.sidebar.SidebarManager;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jspecify.annotations.NonNull;

/**
 * Manages player UI elements including sidebar, action bar, and boss bars
 */
public class PlayerUIManager {
    private static final float MSPT_SMOOTHING_FACTOR = 0.2f;
    private static final int ACTION_BAR_UPDATE_INTERVAL = 4; // Every 200ms (4 ticks)

    private final CustomPlayer player;
    private SidebarManager sidebarManager;

    @Getter
    private final BossBar msptBossBar = BossBar.bossBar(
            Component.text("MSPT: 0 ms").color(NamedTextColor.GREEN),
            0f,
            BossBar.Color.GREEN,
            BossBar.Overlay.PROGRESS
    );

    private float mspt;
    private float smoothedMspt = 0f;

    @Setter
    @Getter
    private boolean showMsptBossbar = false;

    // Cached action bar with dirty flag
    private Component cachedActionBar;
    private boolean actionBarDirty = true;
    private long lastActionBarUpdate = 0;

    public PlayerUIManager(CustomPlayer player) {
        this.player = player;
    }

    public void initializeSidebar() {
        if (sidebarManager == null) {
            sidebarManager = new SidebarManager(player);
        }
    }

    public void showSidebar() {
        if (sidebarManager != null) {
            sidebarManager.show();
        }
    }

    public void updateSidebar() {
        if (sidebarManager != null) {
            sidebarManager.update();
        }
    }

    public void updateMspt(float newMspt) {
        this.mspt = newMspt;
        // Apply exponential smoothing to MSPT
        smoothedMspt = smoothedMspt * (1 - MSPT_SMOOTHING_FACTOR) + mspt * MSPT_SMOOTHING_FACTOR;
    }

    public void tick(long time) {
        // Throttled action bar update
        if (time - lastActionBarUpdate >= ACTION_BAR_UPDATE_INTERVAL) {
            if (actionBarDirty) {
                cachedActionBar = buildActionBarContent();
                actionBarDirty = false;
            }
            player.sendActionBar(cachedActionBar);
            lastActionBarUpdate = time;
        }

        // Update MSPT bossbar if enabled
        if (showMsptBossbar) {
            updateMsptBossbar();
        }

        // Update scoreboard
        updateSidebar();
    }

    public void markActionBarDirty() {
        actionBarDirty = true;
    }

    private @NonNull Component buildActionBarContent() {
        if (player.getPlayerData() == null) {
            return Component.empty();
        }

        Component healthDisplay = Component.text(
                Emojis.HEART.getEmoji() + " " + player.getStatsManager().getCustomHealth() + "/" + player.getPlayerData().getMaxHealth()
        ).color(NamedTextColor.RED);

        Component defenseDisplay = Component.text(
                Emojis.SHIELD.getEmoji() + " " + (int) player.getDefenseStat()
        ).color(NamedTextColor.GRAY);

        Component manaDisplay = Component.text(
                Emojis.STAR.getEmoji() + " " + player.getStatsManager().getCustomMana() + "/" + player.getPlayerData().getMaxMana()
        ).color(NamedTextColor.AQUA);

        Component spacer = Component.text("   ").color(NamedTextColor.GRAY);
        Component actionBar = healthDisplay.append(spacer).append(defenseDisplay).append(spacer).append(manaDisplay);

        // Add streaming/recording status if applicable
        if (player.isStreaming() || player.isRecording()) {
            actionBar = actionBar.append(Component.text("   |   ").color(NamedTextColor.GRAY));
            if (player.isRecording()) {
                actionBar = actionBar.append(Component.text("● Recording").color(NamedTextColor.RED));
            }
            if (player.isStreaming()) {
                if (player.isRecording()) {
                    actionBar = actionBar.append(Component.text(" "));
                }
                actionBar = actionBar.append(Component.text("● Streaming").color(NamedTextColor.LIGHT_PURPLE));
            }
        }

        return actionBar;
    }

    private void updateMsptBossbar() {
        Component title = getMsptBossbarTitle(smoothedMspt);

        // Update bossbar progress (MSPT as progress, 50ms as max for 20 TPS)
        float maxMsptFor20Tps = 50.0f;
        float progress = Math.min(smoothedMspt / maxMsptFor20Tps, 1.0f);

        BossBar.Color barColor;
        if (smoothedMspt > maxMsptFor20Tps) {
            barColor = BossBar.Color.PURPLE;
        } else if (progress < 0.5f) {
            barColor = BossBar.Color.GREEN;
        } else if (progress < 0.8f) {
            barColor = BossBar.Color.YELLOW;
        } else {
            barColor = BossBar.Color.RED;
        }

        msptBossBar.name(title).progress(progress).color(barColor).overlay(BossBar.Overlay.PROGRESS);
    }

    private static @NonNull Component getMsptBossbarTitle(double mspt) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;

        Component msptComponent = Component.text(String.format("MSPT: %.2f ms", mspt)).color(NamedTextColor.GREEN);
        Component separator = Component.text("  |  ").color(NamedTextColor.GRAY);
        Component memoryComponent = Component.text(String.format("RAM: %d/%d MB", usedMemory, maxMemory)).color(NamedTextColor.AQUA);

        return msptComponent.append(separator).append(memoryComponent);
    }
}

