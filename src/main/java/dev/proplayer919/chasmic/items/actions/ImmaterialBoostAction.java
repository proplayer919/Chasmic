package dev.proplayer919.chasmic.items.actions;

import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.player.PlayerStat;
import dev.proplayer919.chasmic.items.ItemActionHandler;
import dev.proplayer919.chasmic.items.ItemActionResult;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ImmaterialBoostAction implements ItemActionHandler {
    @Override
    public ItemActionResult handleAction(CustomPlayer customPlayer) {
        // Give the player a temporary speed boost (+150 speed for 15 seconds)
        customPlayer.addTemporaryStatBonus(PlayerStat.SPEED, 150.0f, 15000);
        customPlayer.sendMessage(Component.text("You feel a surge of immaterial energy! Speed increased for 15 seconds.").color(NamedTextColor.AQUA));
        customPlayer.playSound(Sound.sound(Key.key("item.chorus_fruit.teleport"), Sound.Source.PLAYER, 1.0f, 1.5f));

        return new ItemActionResult(true);
    }
}
