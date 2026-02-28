package dev.proplayer919.chasmic.command;

import dev.proplayer919.chasmic.CustomPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;

/**
 * /performance command for toggling performance overlay
 * Permission: admin.command.performance
 */
public class PerformanceCommand extends Command {

    public PerformanceCommand() {
        super("performance", "perf");

        setCondition((sender, commandString) -> {
            if (sender instanceof CustomPlayer player) {
                // Allow command if player is not yet initialized (to avoid red text)
                // Actual permission check happens in executor
                if (!player.isInitialized()) {
                    return true;
                }
                return player.hasPermission("admin.command.performance");
            }
            return true; // Console always has permission
        });

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof CustomPlayer player)) {
                sender.sendMessage(Component.text("Only players can use this command!", NamedTextColor.RED));
                return;
            }

            boolean newState = !player.isShowMsptBossbar();
            player.setShowMsptBossbar(newState);

            if (newState) {
                player.getMsptBossBar().addViewer(player);
            } else {
                player.getMsptBossBar().removeViewer(player);
            }

            sender.sendMessage(Component.text("Performance overlay " + (newState ? "enabled" : "disabled") + ".", NamedTextColor.GREEN));
        });
    }
}

