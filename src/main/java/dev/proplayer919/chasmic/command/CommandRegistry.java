package dev.proplayer919.chasmic.command;

import dev.proplayer919.chasmic.accessories.AccessoryRegistry;
import dev.proplayer919.chasmic.command.commands.*;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import dev.proplayer919.chasmic.items.CustomItemRegistry;
import dev.proplayer919.chasmic.player.friend.FriendManager;
import dev.proplayer919.chasmic.player.punishment.PunishmentManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;

/**
 * Helper class for registering commands with Minestom's CommandManager
 */
public class CommandRegistry {

    /**
     * Registers all commands with Minestom's command manager
     * @param mongoDBHandler The MongoDB handler for database operations
     * @param punishmentManager The punishment manager for handling punishments
     */
    public static void registerCommands(MongoDBHandler mongoDBHandler, PunishmentManager punishmentManager,
                                       CustomItemRegistry customItemRegistry, AccessoryRegistry accessoryRegistry,
                                       FriendManager friendManager) {
        CommandManager commandManager = MinecraftServer.getCommandManager();

        // Set MongoDB handler for commands that need it
        RankCommand.setMongoDBHandler(mongoDBHandler);
        PermsCommand.setMongoDBHandler(mongoDBHandler);
        SetCurrencyCommand.setMongoDBHandler(mongoDBHandler);
        SetLvlCommand.setMongoDBHandler(mongoDBHandler);

        // Set registries for commands that need them
        CustomGiveCommand.setCustomItemRegistry(customItemRegistry);
        AccessoryGiveCommand.setAccessoryRegistry(accessoryRegistry);

        // Set punishment manager for punishment commands
        WarnCommand.setPunishmentManager(punishmentManager);
        KickCommand.setPunishmentManager(punishmentManager);
        BanCommand.setPunishmentManager(punishmentManager);
        UnbanCommand.setPunishmentManager(punishmentManager);

        // Register commands
        commandManager.register(new MediaCommand());
        commandManager.register(new FriendCommand(friendManager));
        commandManager.register(new DeathNoticeAcknowledgeCommand());

        commandManager.register(new RankCommand());
        commandManager.register(new PermsCommand());

        commandManager.register(new FlyCommand());
        commandManager.register(new GamemodeCommand());

        commandManager.register(new GiveCommand());
        commandManager.register(new CustomGiveCommand());
        commandManager.register(new AccessoryGiveCommand());

        commandManager.register(new CreatureCommand());

        commandManager.register(new WarnCommand());
        commandManager.register(new KickCommand());
        commandManager.register(new BanCommand());
        commandManager.register(new UnbanCommand());

        commandManager.register(new PerformanceCommand());
        commandManager.register(new KillAllCommand());

        commandManager.register(new SetMaxCommand());
        commandManager.register(new HealCommand());
        commandManager.register(new SetCurrencyCommand());
        commandManager.register(new SetLvlCommand());
    }
}
