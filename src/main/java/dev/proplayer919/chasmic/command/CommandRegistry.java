package dev.proplayer919.chasmic.command;

import dev.proplayer919.chasmic.data.MongoDBHandler;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;

/**
 * Helper class for registering commands with Minestom's CommandManager
 */
public class CommandRegistry {

    /**
     * Registers all commands with Minestom's command manager
     * @param mongoDBHandler The MongoDB handler for database operations
     */
    public static void registerCommands(MongoDBHandler mongoDBHandler) {
        CommandManager commandManager = MinecraftServer.getCommandManager();

        // Set MongoDB handler for commands that need it
        RankCommand.setMongoDBHandler(mongoDBHandler);
        PermsCommand.setMongoDBHandler(mongoDBHandler);

        // Register commands
        commandManager.register(new MediaCommand());
        commandManager.register(new RankCommand());
        commandManager.register(new PermsCommand());
        commandManager.register(new FlyCommand());
        commandManager.register(new GamemodeCommand());
        commandManager.register(new GiveCommand());
        commandManager.register(new MobCommand());
    }
}



