package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.command.PermissionCommand;
import dev.proplayer919.chasmic.entities.CustomCreature;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.instance.Instance;

/**
 * /killall command for killing all entities in the instance
 * Permission: admin.command.killall
 */
public class KillAllCommand extends PermissionCommand {

    public KillAllCommand() {
        super("killall", "admin.command.killall");

        setDefaultExecutor((sender, context) -> {
            if (!checkPlayerPermission(sender)) return;

            CustomPlayer player = (CustomPlayer) sender;
            Instance instance = player.getInstance();
            if (instance == null) {
                sender.sendMessage(Component.text("You are not in an instance!", NamedTextColor.RED));
                return;
            }

            // Kill all entities in the instance
            instance.getEntities().forEach(entity -> {
                if (!(entity instanceof CustomCreature)) {
                    // Only kill custom creatures, skip players and other entities
                    return;
                }
                entity.remove();
            });

            sender.sendMessage(Component.text("All entities in the instance have been killed!", NamedTextColor.GREEN));
        });
    }
}
