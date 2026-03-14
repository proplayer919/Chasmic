package dev.proplayer919.chasmic.player.social;

import lombok.Getter;

@Getter
public enum MessagePrivacy {
    EVERYONE("Everyone"),
    FRIENDS_ONLY("Friends Only"),
    NOBODY("Nobody");

    private final String displayName;

    MessagePrivacy(String displayName) {
        this.displayName = displayName;
    }

    public MessagePrivacy next() {
        MessagePrivacy[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}

