package dev.proplayer919.chasmic.sidebar;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.Emojis;
import dev.proplayer919.chasmic.helpers.CurrencyFormatter;
import dev.proplayer919.chasmic.time.ChasmicSeason;
import dev.proplayer919.chasmic.time.ChasmicTime;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.scoreboard.Sidebar;
import org.jspecify.annotations.NonNull;

import java.util.Date;
import java.util.UUID;

/**
 * Manager for handling player sidebars.
 */
public class SidebarManager {

    private final CustomPlayer player;
    private final Sidebar sidebar;
    private long lastUpdate = 0;
    private static final long UPDATE_INTERVAL_MILLIS = 100; // Update every 100ms
    private boolean visible = false;
    private boolean bankLineVisible = false;
    private boolean shardsLineVisible = false;

    public SidebarManager(@NonNull CustomPlayer player) {
        this.player = player;
        this.sidebar = new Sidebar(Component.text("CHASMIC").color(NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, true));
        setupScoreboard();
    }

    /**
     * Sets up the initial scoreboard structure.
     */
    private void setupScoreboard() {
        sidebar.createLine(new Sidebar.ScoreboardLine("serverInfo", buildServerInfoLine(), 13, Sidebar.NumberFormat.blank()));
        sidebar.createLine(new Sidebar.ScoreboardLine("topSpace", Component.text(""), 12, Sidebar.NumberFormat.blank()));

        sidebar.createLine(new Sidebar.ScoreboardLine("date", buildDateLine(), 11, Sidebar.NumberFormat.blank()));
        sidebar.createLine(new Sidebar.ScoreboardLine("time", buildTimeLine(), 10, Sidebar.NumberFormat.blank()));
        sidebar.createLine(new Sidebar.ScoreboardLine("location", buildLocationLine(), 9, Sidebar.NumberFormat.blank()));

        sidebar.createLine(new Sidebar.ScoreboardLine("space1", Component.text(""), 8, Sidebar.NumberFormat.blank()));

        sidebar.createLine(new Sidebar.ScoreboardLine("purse", buildPurseLine(), 7, Sidebar.NumberFormat.blank()));
        syncDynamicCurrencyLines();

        sidebar.createLine(new Sidebar.ScoreboardLine("space2", Component.text(""), 4, Sidebar.NumberFormat.blank()));

        sidebar.createLine(new Sidebar.ScoreboardLine("exp", buildExpLine(), 3, Sidebar.NumberFormat.blank()));

        sidebar.createLine(new Sidebar.ScoreboardLine("space3", Component.text(""), 2, Sidebar.NumberFormat.blank()));

        sidebar.createLine(new Sidebar.ScoreboardLine("year", buildYearLine(), 1, Sidebar.NumberFormat.blank()));
        sidebar.createLine(new Sidebar.ScoreboardLine("address", Component.text("play.chasmic.net").color(NamedTextColor.YELLOW), 0, Sidebar.NumberFormat.blank()));
    }

    /**
     * Shows the sidebar to the player. Call this only after the player is fully in PLAY state.
     */
    public void show() {
        if (visible) {
            return;
        }
        sidebar.addViewer(player);
        visible = true;
    }

    /**
     * Updates the scoreboard with current information.
     * Should be called regularly (e.g., every tick).
     */
    public void update() {
        if (!visible) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        // Only update at specified interval
        if (currentTime - lastUpdate < UPDATE_INTERVAL_MILLIS) {
            return;
        }

        lastUpdate = currentTime;

        sidebar.updateLineContent("date", buildDateLine());
        sidebar.updateLineContent("time", buildTimeLine());
        sidebar.updateLineContent("location", buildLocationLine());
        sidebar.updateLineContent("purse", buildPurseLine());
        syncDynamicCurrencyLines();
        if (bankLineVisible) {
            sidebar.updateLineContent("bank", buildBankLine());
        }
        if (shardsLineVisible) {
            sidebar.updateLineContent("shards", buildShardsLine());
        }
        sidebar.updateLineContent("exp", buildExpLine());
        sidebar.updateLineContent("year", buildYearLine());
    }

    private Component buildServerInfoLine() {
        // DD/MM/YY XXXXXX
        Date date = new Date();
        String dateStr = String.format("%1$td/%1$tm/%1$tY", date);
        return Component.text(dateStr).color(NamedTextColor.GRAY).append(Component.text(" " + UUID.randomUUID().toString().substring(0, 6)).color(NamedTextColor.DARK_GRAY));
    }

    private Component buildDateLine() {
        int dayOfSeason = ChasmicTime.getChasmicDayOfSeason() + 1;
        ChasmicSeason season = ChasmicTime.getCurrentSeason();
        String text = " " + season.getDisplayName() + " " + dayOfSeason + getOrdinalSuffix(dayOfSeason);
        return Component.text(text).color(NamedTextColor.WHITE);
    }

    private Component buildTimeLine() {
        return Component.text(" " + ChasmicTime.getShortFormattedTime()).color(NamedTextColor.GRAY).append(Component.text(" ", NamedTextColor.WHITE)).append(getTimeEmoji());
    }

    private Component buildLocationLine() {
        return Component.text(" ◇ ").color(NamedTextColor.WHITE)
                .append(Component.text("Chasmic City").color(NamedTextColor.AQUA));
    }

    private Component buildPurseLine() {
        String purseStr = CurrencyFormatter.formatCurrency(player.getPlayerData() != null ? player.getPlayerData().getPurse() : 0);
        return Component.text("Purse: ").color(NamedTextColor.WHITE)
                .append(Component.text(purseStr).color(NamedTextColor.GOLD));
    }

    private Component buildBankLine() {
        long bank = player.getPlayerData() != null ? player.getPlayerData().getBank() : 0;
        String bankStr = CurrencyFormatter.formatCurrency(bank);
        return Component.text("Bank: ").color(NamedTextColor.WHITE)
                .append(Component.text(bankStr).color(NamedTextColor.YELLOW));
    }

    private Component buildShardsLine() {
        long shards = player.getPlayerData() != null ? player.getPlayerData().getShards() : 0;
        String shardsStr = CurrencyFormatter.formatCurrency(shards);
        return Component.text("Shards: ").color(NamedTextColor.WHITE)
                .append(Component.text(shardsStr).color(NamedTextColor.AQUA));
    }

    private Component buildExpLine() {
        int level = player.getExpValue() == null ? 0 : player.getExpValue().getLevel();
        return Component.text("Level: ").color(NamedTextColor.WHITE)
                .append(Component.text(level).color(NamedTextColor.LIGHT_PURPLE));
    }

    private Component buildYearLine() {
        return Component.text("Year " + ChasmicTime.getChasmicYear()).color(NamedTextColor.GRAY);
    }

    private String getOrdinalSuffix(int day) {
        if (day % 100 >= 11 && day % 100 <= 13) {
            return "th";
        }

        return switch (day % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }

    private Component getTimeEmoji() {
        // Use different emojis based on time of day (morning, afternoon, evening, night)
        int hour = ChasmicTime.getChasmicHour();
        if (hour >= 5 && hour < 19) {
            return Component.text(Emojis.SUN.getEmoji()).color(NamedTextColor.YELLOW); // Day
        } else {
            return Component.text(Emojis.MOON.getEmoji()).color(NamedTextColor.AQUA); // Night
        }
    }

    private void syncDynamicCurrencyLines() {
        long bank = player.getPlayerData() != null ? player.getPlayerData().getBank() : 0;
        boolean shouldShowBank = bank > 0;
        if (shouldShowBank && !bankLineVisible) {
            sidebar.createLine(new Sidebar.ScoreboardLine("bank", buildBankLine(), 6, Sidebar.NumberFormat.blank()));
            bankLineVisible = true;
        } else if (!shouldShowBank && bankLineVisible) {
            sidebar.removeLine("bank");
            bankLineVisible = false;
        }

        long shards = player.getPlayerData() != null ? player.getPlayerData().getShards() : 0;
        boolean shouldShowShards = shards > 0;
        if (shouldShowShards && !shardsLineVisible) {
            sidebar.createLine(new Sidebar.ScoreboardLine("shards", buildShardsLine(), 5, Sidebar.NumberFormat.blank()));
            shardsLineVisible = true;
        } else if (!shouldShowShards && shardsLineVisible) {
            sidebar.removeLine("shards");
            shardsLineVisible = false;
        }
    }

    /**
     * Cleans up the sidebar
     */
    public void cleanup() {
        if (!visible) {
            return;
        }
        sidebar.removeViewer(player);
        visible = false;
    }
}
