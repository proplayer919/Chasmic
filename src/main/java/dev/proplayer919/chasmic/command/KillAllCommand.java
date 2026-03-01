package dev.proplayer919.chasmic.command;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.entities.CustomCreature;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

/**
 * /killall command for killing all entities in the instance
 * Permission: admin.command.killall
 */
public class KillAllCommand extends Command {

    public KillAllCommand() {
        super("killall");

        setCondition((sender, commandString) -> {
            if (sender instanceof CustomPlayer player) {
                // Allow command if player is not yet initialized (to avoid red text)
                // Actual permission check happens in executor
                if (!player.isInitialized()) {
                    return true;
                }
                return player.hasPermission("admin.command.killall");
            }
            return true; // Console always has permission
        });

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof CustomPlayer player)) {
                sender.sendMessage(Component.text("Only players can use this command!", NamedTextColor.RED));
                return;
            }

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

