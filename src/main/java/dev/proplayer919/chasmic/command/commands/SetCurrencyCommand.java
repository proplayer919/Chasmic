package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.command.PermissionCommand;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.number.ArgumentLong;

/**
 * /setcurrency command for changing player purse, bank, and other currencies
 * Permission: admin.command.setcurrency
 */
public class SetCurrencyCommand extends PermissionCommand {
    @Setter
    private static MongoDBHandler mongoDBHandler;

    public SetCurrencyCommand() {
        super("setcurrency", "admin.command.setcurrency");

        // Arguments
        ArgumentWord currencyArg = ArgumentType.Word("stat")
                .from("purse", "bank", "riftGold", "upgradePoints", "shards");

        ArgumentLong valueArg = (ArgumentLong) ArgumentType.Long("value").between(0L, Long.MAX_VALUE);

        addSyntax((sender, context) -> {
            if (!checkPlayerPermission(sender)) return;

            CustomPlayer player = (CustomPlayer) sender;
            String currencyStr = context.get(currencyArg);
            long value = context.get(valueArg);

            switch(currencyStr) {
                case "purse":
                    player.getPlayerData().setPurse(value);
                    player.sendMessage(Component.text("Your purse has been set to " + value).color(NamedTextColor.GREEN));
                    break;
                case "bank":
                    player.getPlayerData().setBank(value);
                    player.sendMessage(Component.text("Your bank has been set to " + value).color(NamedTextColor.GREEN));
                    break;
                case "riftGold":
                    player.getPlayerData().setRiftGold(value);
                    player.sendMessage(Component.text("Your Rift Gold has been set to " + value).color(NamedTextColor.GREEN));
                    break;
                case "upgradePoints":
                    player.getPlayerData().setUpgradePoints(value);
                    player.sendMessage(Component.text("Your upgrade points have been set to " + value).color(NamedTextColor.GREEN));
                    break;
                case "shards":
                    player.getPlayerData().setShards(value);
                    player.sendMessage(Component.text("Your shards have been set to " + value).color(NamedTextColor.GREEN));
                    break;
                default:
                    player.sendMessage(Component.text("Unknown currency type!").color(NamedTextColor.RED));
            }

            // Save to database
            if (player.getPlayerData() != null && mongoDBHandler != null) {
                mongoDBHandler.savePlayerData(player.getPlayerData());
            }
        }, currencyArg, valueArg);
    }
}

