package dev.proplayer919.chasmic.command;

import dev.proplayer919.chasmic.CustomPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;

/**
 * /setmax command for changing player health and mana maximums
 * Permission: admin.command.setmax
 */
public class SetMaxCommand extends Command {

    public SetMaxCommand() {
        super("setmax");

        setCondition((sender, commandString) -> {
            if (sender instanceof CustomPlayer player) {
                // Allow command if player is not yet initialized (to avoid red text)
                // Actual permission check happens in executor
                if (!player.isInitialized()) {
                    return true;
                }
                return player.hasPermission("admin.command.setmax");
            }
            return true; // Console always has permission
        });

        // Arguments
        ArgumentWord statArg = ArgumentType.Word("stat")
                .from("health", "mana");

        ArgumentInteger maxArg = (ArgumentInteger) ArgumentType.Integer("max").between(1, Integer.MAX_VALUE);

        addSyntax((sender, context) -> {
            String statStr = context.get(statArg);
            int maxValue = context.get(maxArg);

            if (!(sender instanceof CustomPlayer player)) {
                sender.sendMessage(Component.text("Only players can use this command").color(NamedTextColor.RED));
                return;
            }

            if (!player.hasPermission("admin.command.setmax")) {
                sender.sendMessage(Component.text("You don't have permission to use this command").color(NamedTextColor.RED));
                return;
            }

            if (statStr.equalsIgnoreCase("health")) {
                player.setMaxHealth(maxValue);
                player.sendMessage(Component.text("Your maximum health has been set to " + maxValue).color(NamedTextColor.GREEN));
            } else if (statStr.equalsIgnoreCase("mana")) {
                player.setMaxMana(maxValue);
                player.sendMessage(Component.text("Your maximum mana has been set to " + maxValue).color(NamedTextColor.GREEN));
            }
        }, statArg, maxArg);
    }
}

