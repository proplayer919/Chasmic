package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.entities.CreatureTypeRegistry;
import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.Main;
import dev.proplayer919.chasmic.command.PermissionCommand;
import dev.proplayer919.chasmic.entities.CreatureType;
import dev.proplayer919.chasmic.entities.CustomCreature;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * /mpb command for spawning a creature at the player's location
 * Permission: admin.command.creature
 */
public class CreatureCommand extends PermissionCommand {
    private static final Logger logger = LoggerFactory.getLogger(CreatureCommand.class);

    public CreatureCommand() {
        super("creature", "admin.command.creature");

        CreatureTypeRegistry creatureTypeRegistry = Main.getServiceContainer().getCreatureTypeRegistry();

        // Arguments
        String[] allCreatureTypes = creatureTypeRegistry.getAllCreatureTypes().stream().map(CreatureType::id).toArray(String[]::new);
        ArgumentWord creatureTypeArg = new ArgumentWord("type").from(allCreatureTypes);

        ArgumentInteger amountArg = (ArgumentInteger) new ArgumentInteger("amount").setDefaultValue(1);

        // /creature <type> - Spawn creature at player's location
        addSyntax((sender, context) -> {
            if (!checkPlayerPermission(sender)) return;

            CustomPlayer player = (CustomPlayer) sender;
            String mobType = context.get(creatureTypeArg);
            int amount = context.get(amountArg);

            // Check if the amount is greater than 0
            if (amount <= 0) {
                sender.sendMessage(Component.text("Amount must be greater than 0!", NamedTextColor.RED));
                return;
            }

            CreatureType creatureType = creatureTypeRegistry.getCreatureType(mobType);

            if (creatureType == null) {
                sender.sendMessage(Component.text("Unknown creature type: " + mobType, NamedTextColor.RED));
                return;
            }

            Class<? extends CustomCreature> creatureClass = creatureType.creatureClass();

            try {
                for (int i = 0; i < amount; i++) {
                    CustomCreature creature = creatureClass.getDeclaredConstructor().newInstance();
                    creature.setInstance(player.getInstance(), player.getPosition());
                }

                sender.sendMessage(Component.text("Spawned " + amount + "x " + creatureType.name(), NamedTextColor.GREEN));
            } catch (Exception e) {
                sender.sendMessage(Component.text("Failed to spawn creature: " + e.getMessage(), NamedTextColor.RED));
                logger.error("Failed to spawn creature {} for player {}", mobType, player.getUsername(), e);
            }
        }, creatureTypeArg, amountArg);
    }
}

