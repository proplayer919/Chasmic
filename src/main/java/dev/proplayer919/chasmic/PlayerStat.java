package dev.proplayer919.chasmic;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

@Getter
public enum PlayerStat {
    ATTACK("Attack", "⚔", NamedTextColor.RED),
    DEFENSE("Defense", "🛡", NamedTextColor.BLUE),
    CRITICAL_CHANCE("Critical Chance", "✴", NamedTextColor.GOLD),
    SPEED("Speed", "⚡", NamedTextColor.YELLOW),
    FARMING("Farming", "🌾", NamedTextColor.GREEN),;

    private final String statName;
    private final String statIcon;
    private final TextColor statColor;

    PlayerStat(String statName, String statIcon, TextColor statColor) {
        this.statName = statName;
        this.statIcon = statIcon;
        this.statColor = statColor;
    }
}
