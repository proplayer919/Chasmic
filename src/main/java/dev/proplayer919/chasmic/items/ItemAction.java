package dev.proplayer919.chasmic.items;

public record ItemAction(String id, double cooldownSeconds, int manaCost, ItemActionHandler actionHandler) {
}
