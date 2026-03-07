package dev.proplayer919.chasmic.items;

public record ItemAction(String id, String name, String description, double cooldownSeconds, ItemActionType actionType, int manaCost, ItemActionHandler actionHandler) {
}
