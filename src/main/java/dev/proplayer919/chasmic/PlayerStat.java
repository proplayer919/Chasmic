package dev.proplayer919.chasmic;

import lombok.Getter;

@Getter
public enum PlayerStat {
    ATTACK("attack"),
    DEFENSE("defense"),
    CRITICAL_CHANCE("criticalChance");

    private final String statName;

    PlayerStat(String statName) {
        this.statName = statName;
    }
}
