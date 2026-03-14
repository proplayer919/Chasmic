package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.command.PermissionCommand;
import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.player.friend.FriendManager;
import dev.proplayer919.chasmic.player.social.PrivateMessageManager;
import dev.proplayer919.chasmic.player.social.PrivateMessageTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import java.util.UUID;

public class ReplyCommand extends PermissionCommand {

    public ReplyCommand(FriendManager friendManager) {
        super("reply", "command.msg", "r");

        ArgumentStringArray messageArg = ArgumentType.StringArray("message");

        addSyntax((sender, context) -> {
            if (!checkPlayerPermission(sender)) return;

            CustomPlayer player = (CustomPlayer) sender;
            String message = String.join(" ", context.get(messageArg)).trim();

            if (message.isBlank()) {
                player.sendMessage(Component.text("You must include a message.", NamedTextColor.RED));
                return;
            }

            UUID lastContactUuid = PrivateMessageTracker.getLastContact(player.getUuid());
            if (lastContactUuid == null) {
                player.sendMessage(Component.text("You don't have anyone to reply to yet.", NamedTextColor.RED));
                return;
            }

            CustomPlayer recipient = findOnlinePlayer(lastContactUuid);
            if (recipient == null) {
                player.sendMessage(Component.text("That player is no longer online.", NamedTextColor.RED));
                return;
            }

            PrivateMessageManager.sendPrivateMessage(player, recipient, message, friendManager);
        }, messageArg);

        setDefaultExecutor((sender, context) -> sender.sendMessage(Component.text("Usage: /reply <message>", NamedTextColor.RED)));
    }

    private CustomPlayer findOnlinePlayer(UUID uuid) {
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (onlinePlayer instanceof CustomPlayer customPlayer && customPlayer.getUuid().equals(uuid)) {
                return customPlayer;
            }
        }
        return null;
    }
}

