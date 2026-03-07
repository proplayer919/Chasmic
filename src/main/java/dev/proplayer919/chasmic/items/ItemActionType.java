package dev.proplayer919.chasmic.items;

import lombok.Getter;

@Getter
public enum ItemActionType {
    RIGHT_CLICK("RIGHT CLICK"),
    SHIFT_RIGHT_CLICK("SNEAK + RIGHT CLICK");

    private final String displayName;

    ItemActionType(String displayName) {
        this.displayName = displayName;
    }
}
