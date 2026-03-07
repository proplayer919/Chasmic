package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.command.PermissionCommand;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import dev.proplayer919.chasmic.helpers.ExpValue;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentLong;

/**
 * /setexp command for changing player EXP
 * Permission: admin.command.setexp
 */
public class SetExpCommand extends PermissionCommand {
    @Setter
    private static MongoDBHandler mongoDBHandler;

    public SetExpCommand() {
        super("setexp", "admin.command.setexp");

        ArgumentLong expArg = (ArgumentLong) ArgumentType.Long("exp").between(0L, Long.MAX_VALUE);

        addSyntax((sender, context) -> {
            if (!checkPlayerPermission(sender)) return;

            CustomPlayer player = (CustomPlayer) sender;
            long exp = context.get(expArg);

            player.getPlayerData().setCurrentExp(exp);
            player.setExpValue(new ExpValue(exp));

            // Save to database
            if (player.getPlayerData() != null && mongoDBHandler != null) {
                mongoDBHandler.savePlayerData(player.getPlayerData());
            }

            player.sendMessage(Component.text("Your EXP has been set to " + exp + ".", NamedTextColor.GREEN));
        }, expArg);
    }
}

