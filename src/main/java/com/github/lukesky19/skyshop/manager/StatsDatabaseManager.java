package com.github.lukesky19.skyshop.manager;

import java.sql.*;

public class StatsDatabaseManager {
    private final Connection connection;

    /**
     *
     * @param path The path of the database file.
     * @throws SQLException is thrown if the database connection is unable to be created.
     */
    public StatsDatabaseManager(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS stats (" +
                    "material TEXT PRIMARY KEY, " +
                    "buy INTEGER NOT NULL DEFAULT 0, " +
                    "sell INTEGER NOT NULL DEFAULT 0)");
        }
    }

    /**
     * Updates the amount the given material name has been bought or sold in the database.
     * @param material The material name that is being updated.
     * @param buyAmount The amount that was bought.
     * @param sellAmount The amount that was sold.
     * @throws SQLException is thrown if the information failed to save to the database.
     */
    public void updateMaterial(String material, int buyAmount, int sellAmount) throws SQLException {
        if (!materialExists(material)) {
            insertMaterial(material, buyAmount, sellAmount);
        } else {
            if(buyAmount != 0) addBuyAmount(material, buyAmount);
            if(sellAmount != 0) addSellAmount(material, sellAmount);
        }
    }

    /**
     * Checks if the material exists in the database.
     * @param material The material name to check.
     * @return true if it exits, false if not.
     * @throws SQLException is thrown if the information was unable to be retrieved.
     */
    private boolean materialExists(String material) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM stats WHERE material = ?")) {
            preparedStatement.setString(1, material);
            return preparedStatement.executeQuery().next();
        }
    }

    /**
     * Adds a new material name, the buy amount, and the sell amount to the database.
     * @param material The material name to add.
     * @param buyAmount The amount of the material that was bought.
     * @param sellAmount The amount of the material that was sold.
     * @throws SQLException is thrown if the information was unable to be saved.
     */
    private void insertMaterial(String material, int buyAmount, int sellAmount) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO stats (material, buy, sell) VALUES (?, ?, ?)")) {
            preparedStatement.setString(1, material);
            preparedStatement.setInt(2, buyAmount);
            preparedStatement.setInt(3, sellAmount);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * For the given material name, add the amount to how much of said item has been bought.
     * @param material The material name.
     * @param amount The amount that was bought.
     * @throws SQLException is thrown if the information was unable to be saved.
     */
    private void addBuyAmount(String material, int amount) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE stats SET buy = ? WHERE material = ?")) {
            int buyAmount = getBuyAmount(material) + amount;

            preparedStatement.setInt(1, buyAmount);
            preparedStatement.setString(2, material);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * For the given material name, get how many items have been bought.
     * @param material The material name
     * @return an int for how many items have been bought for the given material name.
     * @throws SQLException is thrown if the information was unable to be read.
     */
    public int getBuyAmount(String material) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT buy FROM stats WHERE material = ?")) {
            preparedStatement.setString(1, material);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("buy");
            } else {
                return 0;
            }
        }
    }

    /**
     * For the given material name, add the amount to how much of said item has been sold.
     * @param material The material name.
     * @param amount The amount that was sold.
     * @throws SQLException is thrown if the information was unable to be saved.
     */
    private void addSellAmount(String material, int amount) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE stats SET sell = ? WHERE material = ?")) {
            int sellAmount = getSellAmount(material) + amount;

            preparedStatement.setInt(1, sellAmount);
            preparedStatement.setString(2, material);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * For the given material name, get how many items have been sold.
     * @param material The material name.
     * @return an int for how many items have been sold for the given material name.
     * @throws SQLException is thrown if the information was unable to be read.
     */
    public int getSellAmount(String material) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT sell FROM stats WHERE material = ?")) {
            preparedStatement.setString(1, material);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("sell");
            } else {
                return 0;
            }
        }
    }

    /**
     * Close the database connection.
     * @throws SQLException is thrown if the connection failed to close.
     */
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
