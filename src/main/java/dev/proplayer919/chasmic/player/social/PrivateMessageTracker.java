package dev.proplayer919.chasmic.player.social;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PrivateMessageTracker {
    private static final Map<UUID, UUID> lastContacts = new ConcurrentHashMap<>();

    private PrivateMessageTracker() {
    }

    public static void recordConversation(UUID first, UUID second) {
        if (first == null || second == null || first.equals(second)) {
            return;
        }

        lastContacts.put(first, second);
        lastContacts.put(second, first);
    }

    public static UUID getLastContact(UUID playerUuid) {
        return lastContacts.get(playerUuid);
    }

    public static void clear(UUID playerUuid) {
        UUID lastContact = lastContacts.remove(playerUuid);
        if (lastContact != null) {
            lastContacts.remove(lastContact, playerUuid);
        }
    }
}

