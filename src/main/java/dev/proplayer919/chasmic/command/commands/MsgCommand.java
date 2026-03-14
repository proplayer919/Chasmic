package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.command.PermissionCommand;
import dev.proplayer919.chasmic.command.PlayerNameArgument;
import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.player.friend.FriendManager;
import dev.proplayer919.chasmic.player.social.PrivateMessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class MsgCommand extends PermissionCommand {

    public MsgCommand(FriendManager friendManager) {
        super("msg", "command.msg");

        PlayerNameArgument playerArg = PlayerNameArgument.playerName("player");
        ArgumentStringArray messageArg = ArgumentType.StringArray("message");

        addSyntax((sender, context) -> {
            if (!checkPlayerPermission(sender)) return;

            CustomPlayer player = (CustomPlayer) sender;
            String targetName = context.get(playerArg);
            String message = String.join(" ", context.get(messageArg)).trim();

            if (message.isBlank()) {
                player.sendMessage(Component.text("You must include a message.", NamedTextColor.RED));
                return;
            }

            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);
            if (!(target instanceof CustomPlayer recipient)) {
                player.sendMessage(Component.text("That player must be online to receive private messages.", NamedTextColor.RED));
                return;
            }

            PrivateMessageManager.sendPrivateMessage(player, recipient, message, friendManager);
        }, playerArg, messageArg);

        setDefaultExecutor((sender, context) -> sender.sendMessage(Component.text("Usage: /msg <player> <message>", NamedTextColor.RED)));
    }
}

