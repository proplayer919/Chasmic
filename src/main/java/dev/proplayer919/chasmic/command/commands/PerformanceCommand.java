package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.command.PermissionCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * /performance command for toggling performance overlay
 * Permission: admin.command.performance
 */
public class PerformanceCommand extends PermissionCommand {

    public PerformanceCommand() {
        super("performance", "admin.command.performance", "perf");

        setDefaultExecutor((sender, context) -> {
            if (!checkPlayerPermission(sender)) return;

            CustomPlayer player = (CustomPlayer) sender;
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

