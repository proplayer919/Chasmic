package dev.proplayer919.chasmic.player.friend;

import dev.proplayer919.chasmic.data.MongoDBHandler;
import dev.proplayer919.chasmic.data.PlayerData;
import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.service.ServiceDependencies;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages friend relationships and pending friend requests.
 */
@ServiceDependencies({MongoDBHandler.class})
public class FriendManager {
    private final MongoDBHandler mongoDBHandler;
    private final ConcurrentMap<UUID, Set<UUID>> pendingRequests = new ConcurrentHashMap<>();

    public FriendManager(MongoDBHandler mongoDBHandler) {
        this.mongoDBHandler = mongoDBHandler;
    }

    public enum SendRequestStatus {
        SELF,
        DATA_NOT_READY,
        ALREADY_FRIENDS,
        ALREADY_SENT,
        REQUEST_SENT,
        ACCEPTED_EXISTING_REQUEST
    }

    public enum AcceptRequestStatus {
        DATA_NOT_READY,
        NO_PENDING_REQUEST,
        ALREADY_FRIENDS,
        ACCEPTED
    }

    public enum RemoveFriendStatus {
        DATA_NOT_READY,
        SELF,
        NOT_FOUND,
        NOT_FRIENDS,
        REMOVED
    }

    public record RemoveFriendResult(RemoveFriendStatus status, String displayName, CustomPlayer onlinePlayer) {
    }

    public record FriendListEntry(String username, boolean online) {
    }

    private record TargetLookupResult(PlayerData playerData, CustomPlayer onlinePlayer, String displayName) {
    }

    public SendRequestStatus sendFriendRequest(CustomPlayer requester, CustomPlayer recipient) {
        if (!isDataReady(requester) || !isDataReady(recipient)) {
            return SendRequestStatus.DATA_NOT_READY;
        }

        if (requester.getUuid().equals(recipient.getUuid())) {
            return SendRequestStatus.SELF;
        }

        if (areFriends(requester, recipient)) {
            return SendRequestStatus.ALREADY_FRIENDS;
        }

        if (hasPendingRequest(requester.getUuid(), recipient.getUuid())) {
            return SendRequestStatus.ALREADY_SENT;
        }

        if (hasPendingRequest(recipient.getUuid(), requester.getUuid())) {
            AcceptRequestStatus acceptStatus = acceptFriendRequest(requester, recipient);
            if (acceptStatus == AcceptRequestStatus.ACCEPTED || acceptStatus == AcceptRequestStatus.ALREADY_FRIENDS) {
                return SendRequestStatus.ACCEPTED_EXISTING_REQUEST;
            }
            return SendRequestStatus.DATA_NOT_READY;
        }

        boolean added = pendingRequests
                .computeIfAbsent(recipient.getUuid(), ignored -> ConcurrentHashMap.newKeySet())
                .add(requester.getUuid());

        return added ? SendRequestStatus.REQUEST_SENT : SendRequestStatus.ALREADY_SENT;
    }

    public AcceptRequestStatus acceptFriendRequest(CustomPlayer recipient, CustomPlayer requester) {
        if (!isDataReady(recipient) || !isDataReady(requester)) {
            return AcceptRequestStatus.DATA_NOT_READY;
        }

        if (!hasPendingRequest(requester.getUuid(), recipient.getUuid())) {
            return AcceptRequestStatus.NO_PENDING_REQUEST;
        }

        PlayerData recipientData = recipient.getPlayerData();
        PlayerData requesterData = requester.getPlayerData();
        recipientData.ensureSocialIntegrity();
        requesterData.ensureSocialIntegrity();

        clearPendingRequest(requester.getUuid(), recipient.getUuid());
        clearPendingRequest(recipient.getUuid(), requester.getUuid());

        boolean recipientChanged = addFriend(recipientData, requester.getUuid());
        boolean requesterChanged = addFriend(requesterData, recipient.getUuid());

        if (!recipientChanged && !requesterChanged) {
            return AcceptRequestStatus.ALREADY_FRIENDS;
        }

        persistPlayerData(recipientData);
        persistPlayerData(requesterData);
        return AcceptRequestStatus.ACCEPTED;
    }

    public CompletableFuture<RemoveFriendResult> removeFriend(CustomPlayer player, String targetName) {
        if (!isDataReady(player)) {
            return CompletableFuture.completedFuture(new RemoveFriendResult(RemoveFriendStatus.DATA_NOT_READY, targetName, null));
        }

        CompletableFuture<RemoveFriendResult> resultFuture = new CompletableFuture<>();
        resolveTarget(targetName)
                .thenAccept(target -> runOnNextTick(() -> {
                    if (!player.isOnline() || player.getPlayerData() == null) {
                        resultFuture.complete(new RemoveFriendResult(RemoveFriendStatus.DATA_NOT_READY, targetName, target.onlinePlayer()));
                        return;
                    }

                    if (target.onlinePlayer() != null && target.playerData() == null) {
                        resultFuture.complete(new RemoveFriendResult(RemoveFriendStatus.DATA_NOT_READY, target.displayName(), target.onlinePlayer()));
                        return;
                    }

                    if (target.playerData() == null) {
                        resultFuture.complete(new RemoveFriendResult(RemoveFriendStatus.NOT_FOUND, targetName, null));
                        return;
                    }

                    if (player.getUuid().equals(target.playerData().getUuid())) {
                        resultFuture.complete(new RemoveFriendResult(RemoveFriendStatus.SELF, target.displayName(), target.onlinePlayer()));
                        return;
                    }

                    PlayerData playerData = player.getPlayerData();
                    PlayerData targetData = target.playerData();
                    playerData.ensureSocialIntegrity();
                    targetData.ensureSocialIntegrity();

                    if (!playerData.getFriends().contains(targetData.getUuid())) {
                        resultFuture.complete(new RemoveFriendResult(RemoveFriendStatus.NOT_FRIENDS, target.displayName(), target.onlinePlayer()));
                        return;
                    }

                    removeFriend(playerData, targetData.getUuid());
                    removeFriend(targetData, player.getUuid());
                    clearPendingRequest(player.getUuid(), targetData.getUuid());
                    clearPendingRequest(targetData.getUuid(), player.getUuid());

                    persistPlayerData(playerData);
                    persistPlayerData(targetData);
                    resultFuture.complete(new RemoveFriendResult(RemoveFriendStatus.REMOVED, target.displayName(), target.onlinePlayer()));
                }))
                .exceptionally(throwable -> {
                    resultFuture.completeExceptionally(throwable);
                    return null;
                });

        return resultFuture;
    }

    public CompletableFuture<List<FriendListEntry>> getFriendEntries(CustomPlayer player) {
        if (!isDataReady(player)) {
            return CompletableFuture.completedFuture(List.of());
        }

        PlayerData playerData = player.getPlayerData();
        playerData.ensureSocialIntegrity();

        List<UUID> friendUuids = new ArrayList<>(playerData.getFriends());
        if (friendUuids.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        Map<UUID, CustomPlayer> onlineByUuid = new HashMap<>();
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (onlinePlayer instanceof CustomPlayer customPlayer) {
                onlineByUuid.put(customPlayer.getUuid(), customPlayer);
            }
        }

        List<UUID> offlineFriendUuids = friendUuids.stream()
                .filter(uuid -> !onlineByUuid.containsKey(uuid))
                .toList();

        return mongoDBHandler.loadPlayerDataByUuids(offlineFriendUuids)
                .thenApply(offlineFriends -> {
                    Map<UUID, PlayerData> offlineByUuid = new HashMap<>();
                    for (PlayerData friendData : offlineFriends) {
                        offlineByUuid.put(friendData.getUuid(), friendData);
                    }

                    List<FriendListEntry> entries = new ArrayList<>();
                    for (UUID friendUuid : friendUuids) {
                        CustomPlayer onlineFriend = onlineByUuid.get(friendUuid);
                        if (onlineFriend != null) {
                            entries.add(new FriendListEntry(onlineFriend.getUsername(), true));
                            continue;
                        }

                        PlayerData offlineFriend = offlineByUuid.get(friendUuid);
                        if (offlineFriend != null) {
                            entries.add(new FriendListEntry(offlineFriend.getUsername(), false));
                        }
                    }

                    entries.sort(Comparator
                            .comparing(FriendListEntry::online)
                            .reversed()
                            .thenComparing(FriendListEntry::username, String.CASE_INSENSITIVE_ORDER));
                    return entries;
                });
    }

    public void notifyFriendsOfStatusChange(CustomPlayer player, boolean joined) {
        if (!isDataReady(player)) {
            return;
        }

        Component message = player.buildDisplayName()
                .append(Component.text(joined ? " joined the server." : " left the server.", joined ? NamedTextColor.GREEN : NamedTextColor.RED));

        UUID playerUuid = player.getUuid();
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (!(onlinePlayer instanceof CustomPlayer friend) || friend.getUuid().equals(playerUuid) || friend.getPlayerData() == null) {
                continue;
            }

            friend.getPlayerData().ensureSocialIntegrity();
            if (friend.getPlayerData().getFriends().contains(playerUuid)) {
                friend.sendMessage(message);
            }
        }
    }

    public void clearPendingRequests(UUID playerUuid) {
        pendingRequests.remove(playerUuid);
        pendingRequests.values().forEach(requesters -> requesters.remove(playerUuid));
    }

    private CompletableFuture<TargetLookupResult> resolveTarget(String targetName) {
        Player onlinePlayer = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);
        if (onlinePlayer instanceof CustomPlayer customPlayer) {
            return CompletableFuture.completedFuture(new TargetLookupResult(customPlayer.getPlayerData(), customPlayer, customPlayer.getUsername()));
        }

        return mongoDBHandler.loadPlayerDataByUsername(targetName)
                .thenApply(targetData -> new TargetLookupResult(targetData, null, targetData != null ? targetData.getUsername() : targetName));
    }

    private boolean isDataReady(CustomPlayer player) {
        return player != null && player.getPlayerData() != null;
    }

    private boolean areFriends(CustomPlayer first, CustomPlayer second) {
        first.getPlayerData().ensureSocialIntegrity();
        second.getPlayerData().ensureSocialIntegrity();
        return first.getPlayerData().getFriends().contains(second.getUuid())
                && second.getPlayerData().getFriends().contains(first.getUuid());
    }

    private boolean hasPendingRequest(UUID requesterUuid, UUID recipientUuid) {
        Set<UUID> requesters = pendingRequests.get(recipientUuid);
        return requesters != null && requesters.contains(requesterUuid);
    }

    private void clearPendingRequest(UUID requesterUuid, UUID recipientUuid) {
        Set<UUID> requesters = pendingRequests.get(recipientUuid);
        if (requesters == null) {
            return;
        }

        requesters.remove(requesterUuid);
        if (requesters.isEmpty()) {
            pendingRequests.remove(recipientUuid, requesters);
        }
    }

    private boolean addFriend(PlayerData playerData, UUID friendUuid) {
        playerData.ensureSocialIntegrity();
        if (playerData.getFriends().contains(friendUuid)) {
            return false;
        }

        playerData.getFriends().add(friendUuid);
        return true;
    }

    private void removeFriend(PlayerData playerData, UUID friendUuid) {
        playerData.ensureSocialIntegrity();
        playerData.getFriends().remove(friendUuid);
    }

    private void persistPlayerData(PlayerData playerData) {
        mongoDBHandler.savePlayerData(playerData);
    }

    private void runOnNextTick(Runnable action) {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            action.run();
            return TaskSchedule.stop();
        });
    }
}

