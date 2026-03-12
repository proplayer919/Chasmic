package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.Main;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import dev.proplayer919.chasmic.player.CustomPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;

/**
 * Internal command for acknowledging the death purse-loss reminder.
 */
public class DeathNoticeAcknowledgeCommand extends Command {

    public DeathNoticeAcknowledgeCommand() {
        super("deathnoticeack");

        setCondition((sender, commandString) -> sender instanceof CustomPlayer);

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof CustomPlayer player) || player.getPlayerData() == null) {
                return;
            }

            if (player.getPlayerData().isDeathPurseNoticeAcknowledged()) {
                player.sendMessage(Component.text("Got it. You won't see that reminder again.", NamedTextColor.GRAY));
                return;
            }

            player.getPlayerData().setDeathPurseNoticeAcknowledged(true);

            MongoDBHandler mongoDBHandler = Main.getServiceContainer() != null
                    ? Main.getServiceContainer().getMongoDBHandler()
                    : null;
            if (mongoDBHandler != null) {
                mongoDBHandler.savePlayerData(player.getPlayerData());
            }

            player.sendMessage(Component.text("Got it. You won't see that reminder again.", NamedTextColor.GREEN));
        });
    }
}

