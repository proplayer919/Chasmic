package dev.proplayer919.chasmic.time;

import lombok.Getter;

@Getter
public enum ChasmicSeason {
    SPRING("Spring"),
    SUMMER("Summer"),
    AUTUMN("Autumn"),
    WINTER("Winter");

    private final String displayName;

    ChasmicSeason(String displayName) {
        this.displayName = displayName;
    }

    public static ChasmicSeason fromDayOfYear(int dayOfYear) {
        if (dayOfYear < 25) {
            return SPRING;
        } else if (dayOfYear < 50) {
            return SUMMER;
        } else if (dayOfYear < 75) {
            return AUTUMN;
        } else {
            return WINTER;
        }
    }
}
