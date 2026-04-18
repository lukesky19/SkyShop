/*
    SkyShop is a GUI shop plugin with sell commands, a sell GUI, nested categories, page support, and error checking.
    Copyright (C) 2024 lukeskywlker19

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.github.lukesky19.skyshop.database.table;

import com.github.lukesky19.skylib.common.api.database.parameter.Parameter;
import com.github.lukesky19.skylib.common.api.database.parameter.impl.IntegerParameter;
import com.github.lukesky19.skylib.common.api.database.parameter.impl.LongParameter;
import com.github.lukesky19.skylib.common.api.database.parameter.impl.UUIDParameter;
import com.github.lukesky19.skyshop.database.QueueManager;
import com.github.lukesky19.skyshop.player.data.PlayerData;
import com.github.lukesky19.skyshop.player.data.PlayerModifiers;
import com.github.lukesky19.skyshop.util.ByteArrayParameter;
import org.jspecify.annotations.NonNull;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class handles the players table that stores player data.
 */
public class PlayerDataTable {
    private final @NonNull QueueManager queueManager;
    private final @NonNull VersionsTable versionsTable;
    private final @NonNull String tableName = "skyshop_player_data";

    /**
     * Constructor
     * @param queueManager A {@link QueueManager} instance.
     * @param versionsTable A {@link VersionsTable} instance.
     */
    public PlayerDataTable(
            @NonNull QueueManager queueManager,
            @NonNull VersionsTable versionsTable) {
        this.queueManager = queueManager;
        this.versionsTable = versionsTable;
    }

    /**
     * Creates a table to player data.
     * Queues the table creation and index creation sql.
     * @return A {@link CompletableFuture} of type {@link Void}.
     */
    public @NonNull CompletableFuture<Void> createTable() {
        String tableCreationSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "player_id TEXT PRIMARY KEY NOT NULL UNIQUE, " +
                "version INTEGER NOT NULL, " +
                "prices BLOB NOT NULL, " +
                "last_updated LONG NOT NULL DEFAULT 0)";
        String indexCreationSql = "CREATE INDEX IF NOT EXISTS idx_skyshop_player_data_player_ids ON " + tableName + "(player_id);";

        return queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, indexCreationSql))
                .thenCompose(_ -> versionsTable.updateVersion(tableName, 1));
    }

    /**
     * Loads the player's data from the database.
     * @param playerId The {@link UUID} to load data for.
     * @param playerData The {@link PlayerData} to put data into.
     * @return A {@link CompletableFuture} with {@link PlayerData} when complete. The {@link PlayerData} passed to the method will be updated as well.
     */
    public @NonNull CompletableFuture<@NonNull PlayerData> loadPlayerData(@NonNull UUID playerId, @NonNull PlayerData playerData) {
        String selectSql = "SELECT version, prices FROM " + tableName + " WHERE player_id = ?";
        UUIDParameter uuidParameter = new UUIDParameter(playerId);

        return queueManager.queueReadTransaction(selectSql, List.of(uuidParameter), resultSet -> {
            try {
                if(resultSet.next()) {
                    int version = resultSet.getInt("version");
                    playerData.setVersion(version);

                    byte[] serializedData = resultSet.getBytes("prices");
                    Map<String, PlayerModifiers> deserializedData = deserialize(serializedData);

                    playerData.setPlayerPricesMap(deserializedData);
                }

                return playerData;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Saves the player data for a single player.
     * @param uuid The {@link UUID} of the player.
     * @param playerData The {@link PlayerData} for the player.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NonNull CompletableFuture<Void> savePlayerData(@NonNull UUID uuid, @NonNull PlayerData playerData) {
        String updateSql = "INSERT INTO " + tableName + " (" +
                "player_id, " +
                "version, " +
                "prices, " +
                "last_updated) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (player_id) " +
                "DO UPDATE SET " +
                "version = ?, " +
                "prices = ?, " +
                "last_updated = ? " +
                "WHERE last_updated <= ?";

        UUIDParameter uuidParameter = new UUIDParameter(uuid);
        IntegerParameter versionParameter = new IntegerParameter(playerData.getVersion());
        ByteArrayParameter byteArrayParameter = new ByteArrayParameter(serialize(playerData.getPlayerPricesMap()));
        LongParameter timestampParameter = new LongParameter(System.currentTimeMillis());

        List<Parameter<?>> parameters = List.of(
                uuidParameter,
                versionParameter,
                byteArrayParameter,
                timestampParameter,
                versionParameter,
                byteArrayParameter,
                timestampParameter,
                timestampParameter);

        return queueManager.queueWriteTransaction(updateSql, parameters).thenRun(() -> {});
    }

    /**
     * Saves all player data to the database.
     * @param playerDataMap A {@link Map} mapping {@link UUID}s to {@link PlayerData}.
     * @return A {@link CompletableFuture} of type {@link List} containing {@link Boolean}s when complete. true if successful, and false if not.
     */
    public @NonNull CompletableFuture<@NonNull List<@NonNull Boolean>> savePlayerData(@NonNull Map<@NonNull UUID, @NonNull PlayerData> playerDataMap) {
        if(playerDataMap.isEmpty()) return CompletableFuture.completedFuture(new ArrayList<>());

        List<List<Parameter<?>>> listOfParametersList = new ArrayList<>();
        String updateSql = "INSERT INTO " + tableName + " (" +
                "player_id, " +
                "version, " +
                "prices, " +
                "last_updated) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (player_id) " +
                "DO UPDATE SET " +
                "version = ?, " +
                "prices = ?, " +
                "last_updated = ? " +
                "WHERE last_updated <= ?";

        playerDataMap.forEach((uuid, playerData) -> {
            UUIDParameter uuidParameter = new UUIDParameter(uuid);
            IntegerParameter versionParameter = new IntegerParameter(playerData.getVersion());
            ByteArrayParameter byteArrayParameter;
            byteArrayParameter = new ByteArrayParameter(serialize(playerData.getPlayerPricesMap()));
            LongParameter timestampParameter = new LongParameter(System.currentTimeMillis());

            List<Parameter<?>> parameters = List.of(
                    uuidParameter,
                    versionParameter,
                    byteArrayParameter,
                    timestampParameter,
                    versionParameter,
                    byteArrayParameter,
                    timestampParameter,
                    timestampParameter);

            listOfParametersList.add(parameters);
        });

        return queueManager.queueBulkWriteTransaction(updateSql, listOfParametersList).thenApply(list -> {
                    List<Boolean> results = new ArrayList<>();

                    list.forEach(rowsUpdated -> {
                        if(rowsUpdated > 0) {
                            results.add(true);
                        } else  {
                            results.add(false);
                        }
                    });

                    return results;
                }
        );
    }

    /**
     * Serialize the Map into bytes.
     * @param playerPricesMap The {@link Map} mapping {@link String}s to {@link PlayerModifiers}.
     * @return A byte array.
     */
    private byte[] serialize(@NonNull Map<String, PlayerModifiers> playerPricesMap) {
        try(ByteArrayOutputStream byteOut = new ByteArrayOutputStream(); ObjectOutputStream out = new ObjectOutputStream(byteOut)) {
            out.writeObject(playerPricesMap);
            return byteOut.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deserialize bytes into a {@link Map} that maps {@link String}s to {@link PlayerModifiers}.
     * @param data The bytes to deserialize.
     * @return A {@link Map} mapping {@link String}s to {@link PlayerModifiers}.
     */
    private @NonNull Map<String, PlayerModifiers> deserialize(byte[] data) {
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
             ObjectInputStream in = new ObjectInputStream(byteIn)) {
            return (Map<String, PlayerModifiers>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}