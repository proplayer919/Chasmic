package dev.proplayer919.chasmic.service.module;

import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.player.friend.FriendManager;
import dev.proplayer919.chasmic.player.social.PrivateMessageTracker;
import dev.proplayer919.chasmic.service.Module;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.tag.Tag;
import org.jspecify.annotations.NonNull;

/**
 * Module that notifies players when their friends join or leave.
 */
public class FriendsModule implements Module {
    private static final Tag<Boolean> JOIN_NOTIFIED = Tag.Boolean("friends.joinNotified").defaultValue(false);

    private final FriendManager friendManager;

    public FriendsModule(FriendManager friendManager) {
        this.friendManager = friendManager;
    }

    @Override
    public void attach(@NonNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerTickEvent.class, event -> {
            CustomPlayer player = (CustomPlayer) event.getPlayer();
            if (player.getPlayerData() == null || player.getTag(JOIN_NOTIFIED)) {
                return;
            }

            player.setTag(JOIN_NOTIFIED, true);
            friendManager.notifyFriendsOfStatusChange(player, true);
        });

        eventNode.addListener(PlayerDisconnectEvent.class, event -> {
            CustomPlayer player = (CustomPlayer) event.getPlayer();
            if (player.getPlayerData() != null && player.getTag(JOIN_NOTIFIED)) {
                friendManager.notifyFriendsOfStatusChange(player, false);
            }

            friendManager.clearPendingRequests(player.getUuid());
            PrivateMessageTracker.clear(player.getUuid());
        });
    }

    @Override
    public String getName() {
        return "FriendsModule";
    }
}

