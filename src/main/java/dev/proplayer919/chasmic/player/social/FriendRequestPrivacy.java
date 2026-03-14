package dev.proplayer919.chasmic.player.social;

import lombok.Getter;

@Getter
public enum FriendRequestPrivacy {
    EVERYBODY("Everybody"),
    NOBODY("Nobody");

    private final String displayName;

    FriendRequestPrivacy(String displayName) {
        this.displayName = displayName;
    }

    public FriendRequestPrivacy next() {
        FriendRequestPrivacy[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}

