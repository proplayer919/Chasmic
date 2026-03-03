package dev.proplayer919.chasmic.sidebar;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.helpers.CurrencyFormatter;
import dev.proplayer919.chasmic.time.ChasmicTime;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.scoreboard.Sidebar;
import org.jspecify.annotations.NonNull;

/**
 * Manager for handling player sidebars
 * Displays Chasmic Time, Season/Day, and Currency information
 */
public class SidebarManager {

    private final CustomPlayer player;
    private final Sidebar sidebar;
    private long lastUpdate = 0;
    private static final long UPDATE_INTERVAL_MILLIS = 500; // Update every 500ms
    private boolean visible = false;

    public SidebarManager(@NonNull CustomPlayer player) {
        this.player = player;
        this.sidebar = new Sidebar(Component.text("Chasmic Info").color(NamedTextColor.GOLD));
        setupScoreboard();
    }

    /**
     * Sets up the initial scoreboard structure
     */
    private void setupScoreboard() {
        // Add empty line at top for spacing
        sidebar.createLine(new Sidebar.ScoreboardLine("space1", Component.text(""), 10));

        // Time display
        sidebar.createLine(new Sidebar.ScoreboardLine("timeLabel", Component.text("Time").color(NamedTextColor.YELLOW), 9));
        sidebar.createLine(new Sidebar.ScoreboardLine("time", Component.text("  " + ChasmicTime.getFormattedTimeShort()).color(NamedTextColor.WHITE), 8));

        // Season and day
        sidebar.createLine(new Sidebar.ScoreboardLine("seasonLabel", Component.text("Season").color(NamedTextColor.GREEN), 7));
        sidebar.createLine(new Sidebar.ScoreboardLine("season", Component.text("  " + ChasmicTime.getSeasonAndDayString()).color(NamedTextColor.WHITE), 6));

        // Currency section
        sidebar.createLine(new Sidebar.ScoreboardLine("space2", Component.text(""), 5));
        sidebar.createLine(new Sidebar.ScoreboardLine("currencyLabel", Component.text("Currency").color(NamedTextColor.LIGHT_PURPLE), 4));

        if (player.getPlayerData() != null) {
            String purseStr = CurrencyFormatter.formatCurrency(player.getPlayerData().getPurse());
            sidebar.createLine(new Sidebar.ScoreboardLine("purse", Component.text("  Purse: " + purseStr).color(NamedTextColor.GOLD), 3));

            String bankStr = CurrencyFormatter.formatCurrency(player.getPlayerData().getBank());
            sidebar.createLine(new Sidebar.ScoreboardLine("bank", Component.text("  Bank: " + bankStr).color(NamedTextColor.AQUA), 2));
        }
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
     * Updates the scoreboard with current information
     * Should be called regularly (e.g., every tick)
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

        // Update time
        String formattedTime = ChasmicTime.getFormattedTimeShort();
        sidebar.updateLineContent("time", Component.text("  " + formattedTime).color(NamedTextColor.WHITE));

        // Update season and day
        String seasonAndDay = ChasmicTime.getSeasonAndDayString();
        sidebar.updateLineContent("season", Component.text("  " + seasonAndDay).color(NamedTextColor.WHITE));

        // Update currency if player data is loaded
        if (player.getPlayerData() != null) {
            String purseStr = CurrencyFormatter.formatCurrency(player.getPlayerData().getPurse());
            sidebar.updateLineContent("purse", Component.text("  Purse: " + purseStr).color(NamedTextColor.GOLD));

            String bankStr = CurrencyFormatter.formatCurrency(player.getPlayerData().getBank());
            sidebar.updateLineContent("bank", Component.text("  Bank: " + bankStr).color(NamedTextColor.AQUA));
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
