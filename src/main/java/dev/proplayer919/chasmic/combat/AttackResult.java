package dev.proplayer919.chasmic.combat;

import lombok.Getter;

@Getter
public class AttackResult {
    float damage;
    boolean isCritical;

    AttackResult(float damage, boolean isCritical) {
        this.damage = damage;
        this.isCritical = isCritical;
    }
}
