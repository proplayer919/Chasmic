package dev.proplayer919.chasmic.player;

import dev.proplayer919.chasmic.Emojis;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

@Getter
public enum PlayerStat {
    ATTACK("Attack", Emojis.SWORD.getEmoji(), NamedTextColor.RED),
    DEFENSE("Defense", Emojis.SHIELD.getEmoji(), NamedTextColor.BLUE),
    CRITICAL_CHANCE("Critical Chance", Emojis.STAR_2.getEmoji(), NamedTextColor.GOLD),
    SPEED("Speed", Emojis.BOLT.getEmoji(), NamedTextColor.YELLOW),
    FARMING("Farming", Emojis.FARMING.getEmoji(), NamedTextColor.GREEN),
    INTELLIGENCE("Intelligence", Emojis.STAR.getEmoji(),  NamedTextColor.AQUA),
    HEALTH("Health", Emojis.HEART.getEmoji(),  NamedTextColor.RED);

    private final String statName;
    private final String statIcon;
    private final TextColor statColor;

    PlayerStat(String statName, String statIcon, TextColor statColor) {
        this.statName = statName;
        this.statIcon = statIcon;
        this.statColor = statColor;
    }
}
