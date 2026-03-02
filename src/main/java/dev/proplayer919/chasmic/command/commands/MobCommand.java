package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.command.PermissionCommand;
import dev.proplayer919.chasmic.entities.creatures.Nella;
import dev.proplayer919.chasmic.entities.creatures.TestZombie;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;

/**
 * /mpb command for spawning a mob at the player's location
 * Permission: admin.command.mob
 */
public class MobCommand extends PermissionCommand {

    public MobCommand() {
        super("mob", "admin.command.mob");

        // Arguments
        ArgumentWord mobTypeArg = new ArgumentWord("mob").from("test-zombie", "nella");
        ArgumentInteger amountArg = (ArgumentInteger) new ArgumentInteger("amount").setDefaultValue(1);

        // /mob <mob> - Spawn mob at player's location
        addSyntax((sender, context) -> {
            if (!checkPlayerPermission(sender)) return;

            CustomPlayer player = (CustomPlayer) sender;
            String mobType = context.get(mobTypeArg);
            int amount = context.get(amountArg);


            // Check if the amount is greater than 0
            if (amount <= 0) {
                sender.sendMessage(Component.text("Amount must be greater than 0!", NamedTextColor.RED));
                return;
            }

            switch (mobType) {
                case "test-zombie" -> {
                    // Spawn a test zombie at the player's location
                    for (int i = 0; i < amount; i++) {
                        TestZombie testZombie = new TestZombie();
                        testZombie.setInstance(player.getInstance(), player.getPosition());
                    }
                    sender.sendMessage(Component.text("Spawned " + amount + "x Test Zombies!", NamedTextColor.GREEN));
                }
                case "nella" -> {
                    // Spawn Nella at the player's location
                    for (int i = 0; i < amount; i++) {
                        Nella nella = new Nella();
                        nella.setInstance(player.getInstance(), player.getPosition());
                    }
                    sender.sendMessage(Component.text("Spawned " + amount + "x Nella's!", NamedTextColor.GREEN));
                }
                default -> sender.sendMessage(Component.text("Unknown mob type: " + mobType, NamedTextColor.RED));
            }
        }, mobTypeArg, amountArg);
    }
}

