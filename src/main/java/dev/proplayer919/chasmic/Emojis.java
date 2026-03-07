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
    SHIELD("🛡"),
    PEACE("✌"),
    HOURGLASS("⌛"),
    CLOCK("⌚"),
    CLOUD("☁"),
    UMBRELLA("☂"),
    SNOWFLAKE("❄"),
    PENCIL("✏"),
    CHECKMARK("✔"),
    NOTE("♪");

    private final String emoji;

    Emojis(String emoji) {
        this.emoji = emoji;
    }
}
