package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.command.PermissionCommand;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import dev.proplayer919.chasmic.helpers.ExpValue;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;

import java.math.BigInteger;

/**
 * /setlvl command for changing player EXP level
 * Permission: admin.command.setlvl
 */
public class SetLvlCommand extends PermissionCommand {
    public SetLvlCommand(MongoDBHandler mongoDBHandler) {
        super("setlvl", "admin.command.setlvl");

        ArgumentInteger levelArg = (ArgumentInteger) ArgumentType.Integer("level").between(1, ExpValue.MAX_LEVEL);

        addSyntax((sender, context) -> {
            if (!checkPlayerPermission(sender)) return;

            CustomPlayer player = (CustomPlayer) sender;
            int level = context.get(levelArg);

            BigInteger exp = ExpValue.calculateTotalExpForLevel(level);
            player.getPlayerData().setCurrentExp(exp);
            player.setExpValue(new ExpValue(exp));

            // Save to database
            if (player.getPlayerData() != null && mongoDBHandler != null) {
                mongoDBHandler.savePlayerData(player.getPlayerData());
            }

            player.sendMessage(Component.text("Your level has been set to " + level + ".", NamedTextColor.GREEN));
        }, levelArg);
    }
}
