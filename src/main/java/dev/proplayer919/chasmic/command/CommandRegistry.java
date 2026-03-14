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

        // Register commands
        commandManager.register(new MediaCommand());
        commandManager.register(new FriendCommand(friendManager));
        commandManager.register(new MsgCommand(friendManager));
        commandManager.register(new ReplyCommand(friendManager));
        commandManager.register(new DeathNoticeAcknowledgeCommand());

        commandManager.register(new RankCommand(mongoDBHandler));
        commandManager.register(new PermsCommand(mongoDBHandler));

        commandManager.register(new FlyCommand());
        commandManager.register(new GamemodeCommand());

        commandManager.register(new GiveCommand());
        commandManager.register(new CustomGiveCommand(customItemRegistry));
        commandManager.register(new AccessoryGiveCommand(accessoryRegistry));

        commandManager.register(new CreatureCommand());

        commandManager.register(new WarnCommand(punishmentManager));
        commandManager.register(new KickCommand(punishmentManager));
        commandManager.register(new BanCommand(punishmentManager));
        commandManager.register(new UnbanCommand(punishmentManager));

        commandManager.register(new PerformanceCommand());
        commandManager.register(new KillAllCommand());

        commandManager.register(new HealCommand());
        commandManager.register(new SetCurrencyCommand(mongoDBHandler));
        commandManager.register(new SetLvlCommand(mongoDBHandler));
    }
}
