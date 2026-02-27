package dev.proplayer919.chasmic.data;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import dev.proplayer919.chasmic.PlayerRank;
import lombok.Getter;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Handles MongoDB connections and player data operations
 */
@Getter
public class MongoDBHandler {
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<PlayerData> playerCollection;

    /**
     * Creates a new MongoDB handler with the given connection string
     * @param connectionString MongoDB connection string (e.g., "mongodb://localhost:27017")
     * @param databaseName Name of the database to use
     */
    public MongoDBHandler(String connectionString, String databaseName) {
        // Configure codec registry for POJO support
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );

        // Create MongoDB client with UUID representation
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new com.mongodb.ConnectionString(connectionString))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .codecRegistry(pojoCodecRegistry)
                .build();

        this.mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase(databaseName);
        this.playerCollection = database.getCollection("players", PlayerData.class);
    }

    /**
     * Default constructor using localhost MongoDB
     */
    public MongoDBHandler() {
        this("mongodb://localhost:27017", "chasmic");
    }

    /**
     * Loads player data from the database asynchronously
     * @param uuid The player's UUID
     * @return CompletableFuture with PlayerData, or null if not found
     */
    public CompletableFuture<PlayerData> loadPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            return playerCollection.find(Filters.eq("_id", uuid)).first();
        });
    }

    /**
     * Saves player data to the database asynchronously
     * @param playerData The player data to save
     * @return CompletableFuture that completes when save is done
     */
    public CompletableFuture<Void> savePlayerData(PlayerData playerData) {
        return CompletableFuture.runAsync(() -> {
            playerCollection.replaceOne(
                    Filters.eq("_id", playerData.getUuid()),
                    playerData,
                    new ReplaceOptions().upsert(true)
            );
        });
    }

    /**
     * Creates a new player entry in the database
     * @param uuid Player's UUID
     * @param username Player's username
     * @return The created PlayerData
     */
    public PlayerData createNewPlayer(UUID uuid, String username) {
        long currentTime = System.currentTimeMillis();

        PlayerData playerData = new PlayerData();
        playerData.setUuid(uuid);
        playerData.setUsername(username);
        playerData.setRankId(PlayerRank.DEFAULT.getId());
        playerData.setFirstJoinTimestamp(currentTime);
        playerData.setLastJoinTimestamp(currentTime);
        playerData.setNew(true);

        return playerData;
    }

    /**
     * Loads or creates player data
     * @param uuid Player's UUID
     * @param username Player's username
     * @return CompletableFuture with PlayerData (existing or newly created)
     */
    public CompletableFuture<PlayerData> loadOrCreatePlayerData(UUID uuid, String username) {
        return loadPlayerData(uuid).thenApply(data -> {
            if (data == null) {
                // Player doesn't exist, create new entry
                PlayerData newData = createNewPlayer(uuid, username);
                savePlayerData(newData).join(); // Wait for save to complete
                return newData;
            } else {
                // Player exists, update last join and mark as not new
                data.setLastJoinTimestamp(System.currentTimeMillis());
                data.setNew(false);
                savePlayerData(data); // Save asynchronously
                return data;
            }
        });
    }

    /**
     * Updates a player's rank in the database
     * @param uuid Player's UUID
     * @param rank New rank
     * @return CompletableFuture that completes when update is done
     */
    public CompletableFuture<Void> updatePlayerRank(UUID uuid, PlayerRank rank) {
        return loadPlayerData(uuid).thenCompose(data -> {
            if (data != null) {
                data.setRank(rank);
                return savePlayerData(data);
            }
            return CompletableFuture.completedFuture(null);
        });
    }

    /**
     * Adds a custom permission to a player in the database
     * @param uuid Player's UUID
     * @param permission Permission to add
     * @return CompletableFuture that completes when update is done
     */
    public CompletableFuture<Void> addPlayerPermission(UUID uuid, String permission) {
        return loadPlayerData(uuid).thenCompose(data -> {
            if (data != null) {
                if (!data.getCustomPermissions().contains(permission)) {
                    data.getCustomPermissions().add(permission);
                }
                return savePlayerData(data);
            }
            return CompletableFuture.completedFuture(null);
        });
    }

    /**
     * Removes a custom permission from a player in the database
     * @param uuid Player's UUID
     * @param permission Permission to remove
     * @return CompletableFuture that completes when update is done
     */
    public CompletableFuture<Void> removePlayerPermission(UUID uuid, String permission) {
        return loadPlayerData(uuid).thenCompose(data -> {
            if (data != null) {
                data.getCustomPermissions().remove(permission);
                return savePlayerData(data);
            }
            return CompletableFuture.completedFuture(null);
        });
    }

    /**
     * Closes the MongoDB connection
     */
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

}

