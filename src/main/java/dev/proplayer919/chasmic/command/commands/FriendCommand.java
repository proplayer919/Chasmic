package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.command.PermissionCommand;
import dev.proplayer919.chasmic.command.PlayerNameArgument;
import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.player.friend.FriendManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;

/**
 * /friend command for managing friends and friend requests.
 */
public class FriendCommand extends PermissionCommand {

    public FriendCommand(FriendManager friendManager) {
        super("friend", "command.friend");

        ArgumentWord addArg = ArgumentType.Word("add").from("add");
        ArgumentWord removeArg = ArgumentType.Word("remove").from("remove");
        ArgumentWord listArg = ArgumentType.Word("list").from("list");
        ArgumentWord acceptArg = ArgumentType.Word("accept").from("accept");
        PlayerNameArgument onlinePlayerArg = PlayerNameArgument.playerName("onlinePlayer");
        ArgumentWord playerArg = ArgumentType.Word("player");

        addSyntax((sender, context) -> {
            if (!checkPlayerPermission(sender)) return;

            CustomPlayer player = (CustomPlayer) sender;
            String targetName = context.get(onlinePlayerArg);
            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);

            if (!(target instanceof CustomPlayer customTarget)) {
                sender.sendMessage(Component.text("That player must be online to receive a friend request.", NamedTextColor.RED));
                return;
            }

            FriendManager.SendRequestStatus status = friendManager.sendFriendRequest(player, customTarget);
            switch (status) {
                case SELF -> sender.sendMessage(Component.text("You can't add yourself as a friend.", NamedTextColor.RED));
                case DATA_NOT_READY -> sender.sendMessage(Component.text("One of the players is still loading. Try again in a moment.", NamedTextColor.RED));
                case ALREADY_FRIENDS -> sender.sendMessage(Component.text("You're already friends with ", NamedTextColor.YELLOW)
                        .append(customTarget.buildDisplayName())
                        .append(Component.text(".", NamedTextColor.YELLOW)));
                case RECIPIENT_BLOCKED -> sender.sendMessage(customTarget.buildDisplayName()
                        .append(Component.text(" is not accepting friend requests right now.", NamedTextColor.RED)));
                case ALREADY_SENT -> sender.sendMessage(Component.text("You already sent a friend request to ", NamedTextColor.YELLOW)
                        .append(customTarget.buildDisplayName())
                        .append(Component.text(".", NamedTextColor.YELLOW)));
                case REQUEST_SENT -> {
                    sender.sendMessage(Component.text("Friend request sent to ", NamedTextColor.GREEN)
                            .append(customTarget.buildDisplayName())
                            .append(Component.text(".", NamedTextColor.GREEN)));
                    sendRequestNotification(player, customTarget);
                }
                case ACCEPTED_EXISTING_REQUEST -> sendFriendshipConfirmed(player, customTarget);
            }
        }, addArg, onlinePlayerArg);

        addSyntax((sender, context) -> {
            if (!checkPlayerPermission(sender)) return;

            CustomPlayer player = (CustomPlayer) sender;
            String requesterName = context.get(onlinePlayerArg);
            Player requester = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(requesterName);

            if (!(requester instanceof CustomPlayer customRequester)) {
                sender.sendMessage(Component.text("That player is no longer online.", NamedTextColor.RED));
                return;
            }

            FriendManager.AcceptRequestStatus status = friendManager.acceptFriendRequest(player, customRequester);
            switch (status) {
                case DATA_NOT_READY -> sender.sendMessage(Component.text("One of the players is still loading. Try again in a moment.", NamedTextColor.RED));
                case NO_PENDING_REQUEST -> sender.sendMessage(Component.text("You don't have a pending friend request from ", NamedTextColor.RED)
                        .append(customRequester.buildDisplayName())
                        .append(Component.text(".", NamedTextColor.RED)));
                case ALREADY_FRIENDS, ACCEPTED -> sendFriendshipConfirmed(player, customRequester);
            }
        }, acceptArg, onlinePlayerArg);

        addSyntax((sender, context) -> {
            if (!checkPlayerPermission(sender)) return;

            CustomPlayer player = (CustomPlayer) sender;
            String targetName = context.get(playerArg);

            friendManager.removeFriend(player, targetName)
                    .thenAccept(result -> {
                        switch (result.status()) {
                            case DATA_NOT_READY -> player.sendMessage(Component.text("Player data is still loading. Try again in a moment.", NamedTextColor.RED));
                            case SELF -> player.sendMessage(Component.text("You can't remove yourself from your friends list.", NamedTextColor.RED));
                            case NOT_FOUND -> player.sendMessage(Component.text("No player named '" + targetName + "' was found.", NamedTextColor.RED));
                            case NOT_FRIENDS -> player.sendMessage(Component.text(result.displayName() + " is not on your friends list.", NamedTextColor.RED));
                            case REMOVED -> {
                                player.sendMessage(Component.text("Removed ", NamedTextColor.YELLOW)
                                        .append(Component.text(result.displayName(), NamedTextColor.WHITE))
                                        .append(Component.text(" from your friends list.", NamedTextColor.YELLOW)));

                                if (result.onlinePlayer() != null && result.onlinePlayer().isOnline()) {
                                    result.onlinePlayer().sendMessage(Component.text(player.getUsername(), NamedTextColor.WHITE)
                                            .append(Component.text(" removed you from their friends list.", NamedTextColor.YELLOW)));
                                }
                            }
                        }
                    })
                    .exceptionally(throwable -> {
                        runOnNextTick(() -> player.sendMessage(Component.text("Failed to remove that friend right now.", NamedTextColor.RED)));
                        return null;
                    });
        }, removeArg, playerArg);

        addSyntax((sender, context) -> {
            if (!checkPlayerPermission(sender)) return;

            CustomPlayer player = (CustomPlayer) sender;
            if (player.getPlayerData() == null) {
                player.sendMessage(Component.text("Your friend list is still loading. Try again in a moment.", NamedTextColor.RED));
                return;
            }

            friendManager.getFriendEntries(player)
                    .thenAccept(entries -> runOnNextTick(() -> sendFriendList(player, entries)))
                    .exceptionally(throwable -> {
                        runOnNextTick(() -> player.sendMessage(Component.text("Failed to load your friends list.", NamedTextColor.RED)));
                        return null;
                    });
        }, listArg);

        setDefaultExecutor((sender, context) -> sender.sendMessage(Component.text("Usage: /friend <add|remove|list|accept> [player]", NamedTextColor.RED)));
    }

    private void sendRequestNotification(CustomPlayer requester, CustomPlayer target) {
        Component acceptButton = Component.text("[CLICK TO ACCEPT]", NamedTextColor.GREEN)
                .clickEvent(ClickEvent.runCommand("/friend accept " + requester.getUsername()))
                .hoverEvent(HoverEvent.showText(Component.text("Accept " + requester.getUsername() + "'s friend request", NamedTextColor.YELLOW)));

        target.sendMessage(requester.buildDisplayName()
                .append(Component.text(" sent you a friend request. ", NamedTextColor.YELLOW))
                .append(acceptButton));
    }

    private void sendFriendshipConfirmed(CustomPlayer first, CustomPlayer second) {
        Component firstMessage = Component.text("You are now friends with ", NamedTextColor.GREEN)
                .append(second.buildDisplayName())
                .append(Component.text(".", NamedTextColor.GREEN));

        Component secondMessage = Component.text("You are now friends with ", NamedTextColor.GREEN)
                .append(first.buildDisplayName())
                .append(Component.text(".", NamedTextColor.GREEN));

        first.sendMessage(firstMessage);
        second.sendMessage(secondMessage);
    }

    private void sendFriendList(CommandSender sender, List<FriendManager.FriendListEntry> entries) {
        if (!(sender instanceof CustomPlayer player) || !player.isOnline()) {
            return;
        }

        if (entries.isEmpty()) {
            sender.sendMessage(Component.text("You don't have any friends yet.", NamedTextColor.GRAY));
            return;
        }

        sender.sendMessage(Component.text("Friends (" + entries.size() + "):", NamedTextColor.YELLOW));
        for (FriendManager.FriendListEntry entry : entries) {
            sender.sendMessage(Component.text(" • ", NamedTextColor.DARK_GRAY)
                    .append(Component.text(entry.username(), entry.online() ? NamedTextColor.GREEN : NamedTextColor.GRAY))
                    .append(Component.text(entry.online() ? " (online)" : " (offline)", entry.online() ? NamedTextColor.GREEN : NamedTextColor.DARK_GRAY)));
        }
    }

    private void runOnNextTick(Runnable action) {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            action.run();
            return TaskSchedule.stop();
        });
    }
}

