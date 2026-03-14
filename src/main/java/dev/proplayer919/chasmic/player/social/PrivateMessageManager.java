package dev.proplayer919.chasmic.player.social;

import dev.proplayer919.chasmic.Emojis;
import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.player.friend.FriendManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public abstract class PrivateMessageManager {
    public static void sendPrivateMessage(CustomPlayer sender, CustomPlayer recipient, String message, FriendManager friendManager) {
        if (sender.getUuid().equals(recipient.getUuid())) {
            sender.sendMessage(Component.text("You can't message yourself.", NamedTextColor.RED));
            return;
        }

        if (sender.getPlayerData() == null || recipient.getPlayerData() == null) {
            sender.sendMessage(Component.text("One of the players is still loading. Try again in a moment.", NamedTextColor.RED));
            return;
        }

        sender.getPlayerData().ensureSocialIntegrity();
        recipient.getPlayerData().ensureSocialIntegrity();

        MessagePrivacy privacy = recipient.getPlayerData().getMessagePrivacy();
        if (privacy == MessagePrivacy.NOBODY) {
            sender.sendMessage(recipient.buildDisplayName()
                    .append(Component.text(" is not accepting private messages.", NamedTextColor.RED)));
            return;
        }

        if (privacy == MessagePrivacy.FRIENDS_ONLY && !friendManager.areFriends(sender, recipient)) {
            sender.sendMessage(recipient.buildDisplayName()
                    .append(Component.text(" only accepts private messages from friends.", NamedTextColor.RED)));
            return;
        }

        Component sentMessage = Component.text("You").color(NamedTextColor.BLUE)
                .append(Component.text(" " + Emojis.ARROW_RIGHT.getEmoji() + " ", NamedTextColor.WHITE))
                .append(Component.text(recipient.getUsername(), NamedTextColor.BLUE))
                .append(Component.text(": ", NamedTextColor.GRAY))
                .append(Component.text(message, NamedTextColor.WHITE));

        Component receivedMessage = Component.text(sender.getUsername(), NamedTextColor.BLUE)
                .append(Component.text(" " + Emojis.ARROW_RIGHT.getEmoji() + " ", NamedTextColor.WHITE))
                .append(Component.text("You", NamedTextColor.BLUE))
                .append(Component.text(": ", NamedTextColor.GRAY))
                .append(Component.text(message, NamedTextColor.WHITE));

        sender.sendMessage(sentMessage);
        recipient.sendMessage(receivedMessage);

        PrivateMessageTracker.recordConversation(sender.getUuid(), recipient.getUuid());
    }
}
