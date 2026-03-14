package dev.proplayer919.chasmic;

import lombok.Getter;

@Getter
public enum Emojis {
    SUN("☀"),
    MOON("☽"),
    SMILE("☺"),
    FROWN("☹"),
    SKULL("☠"),
    HEART("❤"),
    STAR("✦"),
    STAR_2("✴"),
    SHIELD("🛡"),
    PEACE("✌"),
    HOURGLASS("⌛"),
    CLOCK("⌚"),
    CLOUD("☁"),
    FARMING("🌾"),
    BOLT("⚡"),
    UMBRELLA("☂"),
    SNOWFLAKE("❄"),
    PENCIL("✏"),
    CHECKMARK("✔"),
    SWORD("⚔"),
    NOTE("♪"),
    ARROW_RIGHT("→");

    private final String emoji;

    Emojis(String emoji) {
        this.emoji = emoji;
    }
}
