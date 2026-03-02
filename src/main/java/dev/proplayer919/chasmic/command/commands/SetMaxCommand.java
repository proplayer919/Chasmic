package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.command.PermissionCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;

/**
 * /setmax command for changing player health and mana maximums
 * Permission: admin.command.setmax
 */
public class SetMaxCommand extends PermissionCommand {

    public SetMaxCommand() {
        super("setmax", "admin.command.setmax");

        // Arguments
        ArgumentWord statArg = ArgumentType.Word("stat")
                .from("health", "mana");

        ArgumentInteger maxArg = (ArgumentInteger) ArgumentType.Integer("max").between(1, Integer.MAX_VALUE);

        addSyntax((sender, context) -> {
            if (!checkPlayerPermission(sender)) return;

            CustomPlayer player = (CustomPlayer) sender;
            String statStr = context.get(statArg);
            int maxValue = context.get(maxArg);


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

