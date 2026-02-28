package dev.proplayer919.chasmic.command;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.entities.creatures.TestZombie;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;

/**
 * /mpb command for spawning a mob at the player's location
 * Permission: admin.command.mob
 */
public class MobCommand extends Command {

    public MobCommand() {
        super("mob");

        setCondition((sender, commandString) -> {
            if (sender instanceof CustomPlayer player) {
                // Allow command if player is not yet initialized (to avoid red text)
                // Actual permission check happens in executor
                if (!player.isInitialized()) {
                    return true;
                }
                return player.hasPermission("admin.command.mob");
            }
            return true; // Console always has permission
        });

        // Arguments
        ArgumentWord mobTypeArg = new ArgumentWord("mob").from("test-zombie");

        // /mob <mob> - Spawn mob at player's location
        addSyntax((sender, context) -> {
            String mobType = context.get(mobTypeArg);

            if (!(sender instanceof CustomPlayer player)) {
                sender.sendMessage(Component.text("Only players can use this command!", NamedTextColor.RED));
                return;
            }

            // Check for permission
            if (!player.hasPermission("admin.command.mob")) {
                sender.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
                return;
            }

            switch (mobType) {
                case "test-zombie" -> {
                    // Spawn a test zombie at the player's location
                    TestZombie testZombie = new TestZombie();
                    testZombie.setInstance(player.getInstance(), player.getPosition());
                    sender.sendMessage(Component.text("Spawned a test zombie!", NamedTextColor.GREEN));
                }
                default -> sender.sendMessage(Component.text("Unknown mob type: " + mobType, NamedTextColor.RED));
            }
        }, mobTypeArg);
    }
}

